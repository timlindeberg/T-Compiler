package tcompiler
package code

import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream}

import cafebabe.AbstractByteCodes._
import cafebabe.ByteCodes._
import cafebabe.ClassFileTypes._
import cafebabe.Flags._
import cafebabe._
import org.objectweb.asm.{ClassReader, ClassWriter}
import tcompiler.analyzer.Symbols._
import tcompiler.analyzer.Types._
import tcompiler.ast.Trees._
import tcompiler.utils.Extensions._
import tcompiler.utils._

import scala.collection.mutable

object CodeGeneration extends Pipeline[List[CompilationUnit], Unit] with Colored {

  import CodeGenerator._

  var useColor = false

  def run(ctx: Context)(cus: List[CompilationUnit]): Unit = {
    val classes = cus.flatMap(_.classes)

    // output code in parallell?
    useColor = ctx.useColor
    if (shouldPrintCode(ctx)) {
      val stageName = Blue(CodeGeneration.stageName)
      println(s"${Bold}Output after $Reset$stageName:\n")
    }
    val outputFiles = classes.flatMap(generateClassFile(_, ctx))
    // TODO: this should be done for one file and then be copied
    outputFiles foreach generateStackMapFrames
  }

  /** Writes the proper .class file in a given directory. An empty string for dir is equivalent to "./". */
  private def generateClassFile(classDecl: ClassDeclTree, ctx: Context): List[String] = {
    val classFile = makeClassFile(classDecl)
    classDecl.fields.foreach { varDecl =>
      val varSymbol = varDecl.getSymbol
      val flags = getFieldFlags(varDecl)
      classFile.addField(varSymbol.getType.byteCodeName, varSymbol.name).setFlags(flags)
    }

    initializeStaticFields(classDecl, classFile)
    val classSymbol = classDecl.getSymbol

    classDecl.methods.foreach { methodDecl =>
      val methSymbol = methodDecl.getSymbol

      val methodHandle = methodDecl match {
        case methDecl: MethodDecl =>
          val argTypes = methSymbol.argList.map(_.getType.byteCodeName).mkString
          val methDescriptor = methodDescriptor(methSymbol)
          classFile.addMethod(methSymbol.getType.byteCodeName, methSymbol.name, argTypes, methDescriptor)

        case con: ConstructorDecl =>
          generateConstructor(Some(con), classFile, classDecl)
      }
      val flags = getMethodFlags(methodDecl)
      methodHandle.setFlags(flags)
      methSymbol.annotations foreach methodHandle.addAnnotation

      if (!methodDecl.isAbstract) {
        val ch = generateMethod(methodHandle, methodDecl)

        // If a method is overriden but with another return type
        // a bridge method needs to be generated
        classSymbol.overriddenMethod(methSymbol)
          .filter(_.getType != methSymbol.getType)
          .ifDefined { overriden =>
            val flags = METHOD_ACC_PUBLIC | METHOD_ACC_BRIDGE | METHOD_ACC_SYNTHETIC

            val thisTree = This().setSymbol(classSymbol).setType(TObject(classSymbol))
            generateBridgeMethod(classFile, overriden, methSymbol, flags, thisTree)
          }

        if (shouldPrintCode(ctx))
          println(ch.stackTrace(ctx.useColor))
      }

    }

    if (!classDecl.methods.exists(_.isInstanceOf[ConstructorDecl]))
      generateDefaultConstructor(classFile, classDecl)

    // TODO: Generate methods so that toString etc. can be defined in an interface
    /*
    if(!classSymbol.isAbstract){
      Types.ObjectSymbol.methods.foreach { objMeth =>
        if(!classSymbol.methods.exists(m => m.name == objMeth.name && m.argTypes == objMeth.argTypes)){
          classSymbol.implementingMethod(objMeth)
            .filter(_.classSymbol.isAbstract)
            .ifDefined{ methodInTrait =>
              // Object method is not defined in the class but is defined in a parent trait
              // A bridge method needs to be generated.
              val trai = methodInTrait.classSymbol
              val base = Super(Some(ClassID(trai.name).setSymbol(trai))).setType(TObject(trai))
              generateBridgeMethod(classFile, objMeth, methodInTrait, METHOD_ACC_PUBLIC, base)
            }
        }
      }
    }
    */


    val className = classDecl.getSymbol.name
    val files = ctx.outDirs.map(getFilePath(_, className))
    files.foreach(classFile.writeToFile)
    files
  }

  private def generateBridgeMethod(classFile: ClassFile, overriden: MethodSymbol, meth: MethodSymbol, flags: U2, base: ExprTree) = {
    val argTypes = overriden.argList.map(_.getType.byteCodeName).mkString
    val retType = overriden.getType.byteCodeName

    val descriptor = methodDescriptor(overriden)
    val mh = classFile.addMethod(retType, overriden.name, argTypes, descriptor)
    mh.setFlags(flags)

    val args = overriden.argList.map(arg => VariableID(arg.name).setSymbol(arg))

    // The code to generate in the bridge method
    val methType = meth.getType
    val methodID = MethodID(meth.name).setSymbol(meth)
    val methodCall = MethodCall(methodID, args).setType(methType)
    val access = NormalAccess(base, methodCall).setType(methType)
    val code = Return(Some(access)).setType(methType)

    val localVariableMap = constructVariableMap(overriden)

    val ch = mh.codeHandler
    val codeGenerator = new CodeGenerator(ch, localVariableMap)
    codeGenerator.compileStat(code)
    ch.use(_.freeze)
  }

  private def generateStackMapFrames(file: String) = {
    // Use ASM libary to generate the stack map frames
    // since Cafebabe does not support this.
    val classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES)
    val inputStream = new BufferedInputStream(new FileInputStream(file))
    val classReader = new ClassReader(inputStream)

    classReader.accept(classWriter, ClassReader.SKIP_FRAMES)
    inputStream.close()
    val bytes = classWriter.toByteArray

    val fileStream = new FileOutputStream(file)
    fileStream.write(bytes)
    fileStream.close()
  }

  private def makeClassFile(classDecl: ClassDeclTree) = {
    val classSymbol = classDecl.getSymbol
    val parents = classSymbol.parents
    val className = classSymbol.name

    val (parent, traits) = if (classSymbol.isAbstract)
      (None, parents)
    else if (parents.isEmpty)
      (None, List())
    else if (parents.head.isAbstract)
      (None, parents)
    else
      (Some(parents.head.name), parents.drop(1))

    val classFile = new ClassFile(className, parent)
    traits.foreach(t => classFile.addInterface(t.name))
    classFile.setSourceFile(classDecl.file.getName)

    val flags = if (classSymbol.isAbstract) TraitFlags else ClassFlags
    classFile.setFlags(flags)
    // Default is public

    classFile
  }

  private def generateMethod(mh: MethodHandler, methTree: MethodDeclTree): CodeHandler = {
    val localVariableMap = constructVariableMap(methTree.getSymbol)

    val ch = mh.codeHandler
    val codeGenerator = new CodeGenerator(ch, localVariableMap)
    codeGenerator.compileStat(methTree.stat.get)
    addReturnStatement(ch, methTree.getSymbol)
    ch.use(_.freeze)
  }

  private def constructVariableMap(meth: MethodSymbol): mutable.Map[VariableSymbol, Int] = {
    var offset = if (meth.isStatic) 0 else 1

    mutable.Map() ++ meth.argList.zipWithIndex.map { case (arg, i) =>
        val pair = arg -> (i + offset)
        // Longs and doubles take up two slots
        if (arg.getType.size == 2)
          offset += 1
        pair
    }.toMap
  }

  private def generateDefaultConstructor(classFile: ClassFile, classDecl: ClassDeclTree): Unit = {
    if (classDecl.getSymbol.isAbstract)
      return

    val mh = generateConstructor(None, classFile, classDecl)
    val ch = mh.codeHandler
    ch << RETURN
    ch.freeze
  }

  private def generateConstructor(con: Option[ConstructorDecl], classFile: ClassFile, classDecl: ClassDeclTree): MethodHandler = {
    val mh = con match {
      case Some(conDecl) =>
        val argTypes = conDecl.getSymbol.argList.map(_.getType.byteCodeName).mkString
        val methDescriptor = methodDescriptor(conDecl.getSymbol)
        classFile.addConstructor(argTypes, methDescriptor)
      case _             =>
        classFile.addConstructor("", "new()")
    }

    initializeNonStaticFields(classDecl, mh.codeHandler)
    addSuperCall(mh, classDecl)
    mh
  }

  private def methodDescriptor(methSym: MethodSymbol) = {
    methSym.classSymbol.name + "." + methSym.signature + ":" + methSym.byteCodeSignature
  }

  private def initializeStaticFields(classDecl: ClassDeclTree, classFile: ClassFile): Unit = {
    val staticFields = classDecl.fields.filter(v => v.init.isDefined && v.isStatic)
    if (staticFields.isEmpty)
      return

    // TODO: why lazy?
    lazy val ch: CodeHandler = classFile.addClassInitializer.codeHandler
    val codeGenerator = new CodeGenerator(ch, mutable.HashMap())
    staticFields.foreach { case varDecl@VarDecl(varTpe, id, Some(expr), _) =>
      compileField(expr, id, classDecl, ch, codeGenerator)
    }
    ch << RETURN
    ch.freeze
  }

  private def initializeNonStaticFields(classDecl: ClassDeclTree, ch: CodeHandler) = {
    val nonStaticFields = classDecl.fields.filter(v => v.init.isDefined && !v.isStatic)
    val codeGenerator = new CodeGenerator(ch, mutable.HashMap())
    nonStaticFields foreach { case varDecl@VarDecl(_, id, Some(expr), _) =>
      ch << ArgLoad(0) // put this-reference on stack
      compileField(expr, id, classDecl, ch, codeGenerator)
    }
  }

  private def compileField(expr: ExprTree, id: VariableID, classDecl: ClassDeclTree, ch: CodeHandler, codeGenerator: CodeGenerator) = {
    codeGenerator.compileExpr(expr)
    val sym = id.getSymbol
    val className = classDecl.getSymbol.name
    val fieldName = id.getSymbol.name
    val typeName = sym.getType.byteCodeName
    if (sym.isStatic)
      ch << PutStatic(className, fieldName, typeName)
    else
      ch << PutField(className, fieldName, typeName)

  }

  private def getMethodFlags(method: MethodDeclTree) = {
    var flags: U2 = 0

    method.modifiers.foreach {
      case Public()    => flags |= METHOD_ACC_PUBLIC
      case Private()   => flags |= METHOD_ACC_PRIVATE
      case Protected() => flags |= METHOD_ACC_PROTECTED
      case Static()    => flags |= METHOD_ACC_STATIC
      case _           =>
    }
    if (method.isAbstract)
      flags |= METHOD_ACC_ABSTRACT

    flags
  }

  private def getFieldFlags(varDecl: VarDecl) = {
    var flags: U2 = 0

    varDecl.modifiers.foreach {
      case Public()    => flags |= FIELD_ACC_PUBLIC
      case Private()   => flags |= FIELD_ACC_PRIVATE
      case Protected() => flags |= FIELD_ACC_PROTECTED
      case Static()    => flags |= FIELD_ACC_STATIC
      case Final()     => flags |= FIELD_ACC_FINAL
      case _           =>
    }
    flags
  }

  private def getFilePath(outDir: File, className: String): String = {
    var prefix = outDir.getAbsolutePath.replaceAll("\\\\", "/")

    // Weird Windows behaviour
    if(prefix.endsWith("."))
      prefix = prefix.dropRight(1)

    prefix += "/"
    val split = className.split("/")
    val packageDir = split.dropRight(1).mkString("/")
    val filePath = prefix + packageDir
    val f = new File(filePath)
    if (!f.getAbsoluteFile.exists() && !f.mkdirs())
      sys.error(s"Could not create output directory '${f.getAbsolutePath}'.")


    prefix + className + ".class"
  }

  private def addReturnValueAndStatement(ch: CodeHandler, tpe: Type) = tpe match {
    case TUnit => ch << RETURN
    case _     =>
      tpe.codes.defaultConstant(ch)
      tpe.codes.ret(ch)
  }

  private def addReturnStatement(ch: CodeHandler, methodSymbol: MethodSymbol) = ch.lastRealInstruction match {
    case Some(byteCode) =>
      byteCode match {
        case ARETURN | IRETURN | RETURN | DRETURN | FRETURN | LRETURN =>
        case _                                                        =>
          addReturnValueAndStatement(ch, methodSymbol.getType)
      }
    case None           => ch << RETURN
  }

  private def addSuperCall(mh: MethodHandler, classDecl: ClassDeclTree) = {
    val superClassName = classDecl.getSymbol.parents match {
      case (c: ClassSymbol) :: _ => if (c.isAbstract) JavaObject else c.name
      case Nil                   => JavaObject
    }

    mh.codeHandler << ALOAD_0
    mh.codeHandler << InvokeSpecial(superClassName, CodeGenerator.ConstructorName, "()V")
  }

  private def shouldPrintCode(ctx: Context) = ctx.printCodeStages.contains(CodeGeneration.stageName)

}


