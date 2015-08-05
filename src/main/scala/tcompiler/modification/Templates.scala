package tcompiler
package modification

import com.rits.cloning._
import tcompiler.ast.{Printer, Trees}
import tcompiler.ast.Trees._
import tcompiler.utils.{Context, Pipeline, Positioned}

import scala.collection.mutable.ArrayBuffer

object Templates extends Pipeline[Program, Program] {
  def run(ctx: Context)(prog: Program): Program = {
    import ctx.reporter._

    val templateClasses = prog.classes.filter(_.id.isTemplated) ::: Imports.importGenericClasses(prog, ctx)

    /* Error messages and predefined
     * error types to return in case of errors. */

    val ErrorMap = Map[TypeTree, TypeTree]()
    val ErrorType = new ClassIdentifier("ERROR")
    val ErrorClass = new InternalClassDecl(ErrorType, None, List(), List())

    def ErrorWrongNumGenerics(expected: Int, found: Int, pos: Positioned) = {
      error("Wrong number of generic parameters, expected " + expected + " but found " + found, pos)
      ErrorMap
    }

    def ErrorNewPrimitive(name: String, pos: Positioned) = {
      error("Cannot create a new instance of primitive type \'" + name + "\'.", pos)
      ErrorType
    }

    def ErrorDoesNotExist(name: String, pos: Positioned) = {
      error("No template class named \'" + name + "\'.", pos)
      ErrorClass
    }

    def ErrorSameName(name: String, pos: Positioned) = {
      error("Generic identifiers with the same name: \'" + name + "\'", pos)
    }

    object ClassGenerator {

      val cloner = new Cloner
      cloner.registerConstant(Nil)
      cloner.registerConstant(None)
      cloner.registerConstant(Private)
      cloner.registerConstant(Public)
      cloner.registerConstant(Protected)
      cloner.registerConstant(Static)

      var generated: Set[String] = Set()
      var generatedClasses: ArrayBuffer[ClassDecl] = ArrayBuffer()

      def generate(prog: Program): List[ClassDecl] = {

        def generateIfTemplated(tpe: TypeTree): Unit = Some(tpe) collect {
          case x: ClassIdentifier if x.isTemplated =>
            x.templateTypes.foreach(generateIfTemplated)
            generateClass(x)
        }

        def collect(f: Product, p: Product) = Some(p) collect {
          case c: ClassDecl => c.parent.foreach(generateIfTemplated)
          case v: VarDecl   => generateIfTemplated(v.tpe)
          case f: Formal    => generateIfTemplated(f.tpe)
          case n: New       => generateIfTemplated(n.tpe)
        }

        Trees.traverse(prog.classes.filter(!_.id.isTemplated), collect)
        Trees.traverse(prog.main, collect)
        generatedClasses.toList
      }

      private def generateClass(typeId: ClassIdentifier): Unit = {
        if (generated(typeId.templatedClassName))
          return

        generated += typeId.templatedClassName

        templateClasses.find(_.id.value == typeId.value) match {
          case Some(template) => generatedClasses += newTemplateClass(template, typeId.templateTypes)
          case None           => ErrorDoesNotExist(typeId.value, typeId)
        }
      }

      private def newTemplateClass(template: ClassDecl, templateTypes: List[TypeTree]): ClassDecl = {
        val templateMap = constructTemplateMapping(template.id.templateTypes, templateTypes)

        /* Helper functions to perform transformation */
        def updateType(t: TypeTree): TypeTree = {
          Some(t) collect {
            case t@ClassIdentifier(_, templateTypes) if t.isTemplated =>
              t.templateTypes = templateTypes.map(updateType)
              generateClass(t)
            case a@ArrayType(tpe)                                     => a.tpe = updateType(tpe)
          }
          templateMap.getOrElse(t, t)
        }

        def updateTypeOfNewExpr(newExpr: New) = updateType(newExpr.tpe) match {
          case t: ClassIdentifier => t
          case t                  => ErrorNewPrimitive(t.name, newExpr.tpe)
        }

        def templateName(id: ClassIdentifier) =
          id.copy(value = template.id.templatedClassName(templateTypes), templateTypes = List())

        val newClass = cloner.deepClone(template)
        Trees.traverse(newClass, (_, current) => Some(current) collect {
          case c: ClassDecl       => c.id = templateName(c.id)
          case v: VarDecl         => v.tpe = updateType(v.tpe)
          case f: Formal          => f.tpe = updateType(f.tpe)
          case m: MethodDecl      => m.retType = updateType(m.retType)
          case c: ConstructorDecl => c.id = Identifier(template.id.templatedClassName(templateTypes))
          case o: OperatorDecl    => o.retType = updateType(o.retType)
          case n: New             => n.tpe = updateTypeOfNewExpr(n)
          case n: NewArray        => n.tpe = updateType(n.tpe)
        })
        newClass
      }

      private def constructTemplateMapping(templateList: List[TypeTree], templateTypes: List[TypeTree]): Map[TypeTree, TypeTree] = {
        val diff = templateTypes.size - templateList.size
        if (diff != 0) {
          val index = if (diff > 0) templateList.size else templateTypes.size - 1
          ErrorWrongNumGenerics(templateList.size, templateTypes.size, templateTypes(index))
        } else {
          templateList.zip(templateTypes).toMap
        }
      }
    }

    def checkTemplateClassDefs(templateClasses: List[ClassDecl]) =
      templateClasses.foreach { x =>
        var set = Set[TypeTree]()
        var reportedFor = Set[TypeTree]()
        x.id.templateTypes.foreach { x =>
          if (set(x) && !reportedFor(x)) {
            ErrorSameName(x.name, x)
            reportedFor += x
          }
          set += x
        }
      }

    def replaceTypes(prog: Program): Program = {
      def replaceType(tpe: TypeTree) = tpe match {
        case x: ClassIdentifier => replaceTypeId(x)
        case x                  => x
      }

      def replaceTypeId(tpe: ClassIdentifier) =
        if (tpe.isTemplated) new ClassIdentifier(tpe.templatedClassName).setPos(tpe)
        else tpe

      Trees.traverse(prog, (_, curr) => Some(curr) collect {
        case c: ClassDecl    => c.parent = c.parent.map(replaceTypeId)
        case m: MethodDecl   => m.retType = replaceType(m.retType)
        case o: OperatorDecl => o.retType = replaceType(o.retType)
        case v: VarDecl      => v.tpe = replaceType(v.tpe)
        case f: Formal       => f.tpe = replaceType(f.tpe)
        case n: New          => n.tpe = replaceTypeId(n.tpe)
      })
      prog
    }

    checkTemplateClassDefs(templateClasses)
    val newClasses = ClassGenerator.generate(prog)
    val oldClasses = prog.classes.filter(!_.id.isTemplated)
    val newProg = prog.copy(classes = oldClasses ++ newClasses)

    replaceTypes(newProg)
  }
}
