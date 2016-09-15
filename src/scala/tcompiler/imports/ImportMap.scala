package tcompiler.imports

import tcompiler.analyzer.Symbols.ExtensionClassSymbol
import tcompiler.ast.Trees._
import tcompiler.utils.Context
import tcompiler.utils.Extensions._

import scala.collection.mutable

/**
  * Created by Tim Lindeberg on 7/5/2016.
  */

class ImportMap(override var ctx: Context) extends ImportErrors {


  override var importMap   = this
  private  val shortToFull = mutable.Map[String, String]()
  private  val fullToShort = mutable.Map[String, String]()

  var imports         : List[Import]               = Nil
  var extensionSymbols: List[ExtensionClassSymbol] = Nil

  private val javaObject = List("java", "lang", "Object")
  private val javaString = List("java", "lang", "String")
  private val koolLang = List("kool", "lang")
  private val DefaultImports = List[Import](
    RegularImport(javaObject),
    RegularImport(javaString),
    ExtensionImport(koolLang, javaObject)
    //ExtensionImport(koolLang, javaString)
  )

  def this() = this(null)
  def this(imports: List[Import], pack: Package, classes: List[ClassDeclTree], ctx: Context) {
    this(ctx)
    this.imports = imports

    DefaultImports foreach addImport
    imports foreach addImport

    val packName = pack.name
    if (packName != ""){
      classes.filterNotType[ExtensionDecl] foreach { c =>
        val className = c.id.name
        addImport(className, s"$packName.$className")
      }
    }
  }

  private def addImport(imp: Import): Unit = imp match {
    case regImp: RegularImport            =>
      val fullName = regImp.name
      val shortName = regImp.shortName
      val templateImporter = new TemplateImporter(ctx)

      if (contains(shortName))
        ErrorConflictingImport(regImp.writtenName, getFullName(shortName), regImp)
      else if (!(templateImporter.classExists(fullName) || ClassSymbolLocator.classExists(fullName)))
        ErrorCantResolveImport(regImp.writtenName, regImp)
      else
        addImport(shortName, fullName)
    case extensionImport: ExtensionImport =>
      ClassSymbolLocator.findExtensionSymbol(extensionImport.fullName) match {
        case Some(e) => extensionSymbols ::= e
        case None    => ErrorCantResolveExtensionsImport(extensionImport, extensionImport)
      }
    case wildCardImport: WildCardImport => ??? // TODO: Support wild card imports.
  }

  def getExtensionClasses(className: String) =
    extensionSymbols.filter { extSym =>
      val name = extSym.name.replaceAll(""".*\$EX\/""", "")
      name == className
    }

  def addExtensionClass(extensionClassSymbol: ExtensionClassSymbol) = extensionSymbols ::= extensionClassSymbol

  def addImport(tup: (String, String)): Unit = addImport(tup._1, tup._2)
  def addImport(short: String, full: String): Unit = {
    val f = full.replaceAll("::", ".").replaceAll("/", ".")
    shortToFull += short -> f
    fullToShort += f -> short
  }

  def importNames = imports map importName

  def importName(imp: Import): String = getFullName(imp.name)

  def importName(typeId: ClassID): String = {
    val name = typeId.name.replaceAll("::", ".")
    getFullName(name)
  }

  def importEntries = shortToFull.values.toList

  def getFullName(shortName: String) = shortToFull.getOrElse(shortName, shortName).replaceAll("::", ".")
  def getShortName(fullName: String) = fullToShort.getOrElse(fullName.replaceAll("::", "."), fullName)

  def getErrorName(name: String) = {
    var s = name
    for (e <- fullToShort)
      s = s.replaceAll(e._1, e._2)
    s.replaceAll("/", "::")
  }

  def contains(shortName: String) = shortToFull.contains(shortName)

  def entries = shortToFull.iterator

  override def toString = {
    shortToFull.map { case (short, full) => s"$short -> $full" }.mkString("\n")
  }

}