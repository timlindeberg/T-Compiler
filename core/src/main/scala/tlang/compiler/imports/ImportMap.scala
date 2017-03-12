package tlang.compiler.imports

import tlang.compiler.Context
import tlang.compiler.analyzer.Symbols.ExtensionClassSymbol
import tlang.compiler.ast.Trees._
import tlang.utils.Extensions._
import tlang.utils.NoPosition

import scala.collection.mutable

/**
  * Created by Tim Lindeberg on 7/5/2016.
  */

case class ImportMap(ctx: Context,
  imports: List[Import] = Nil,
  pack: Package = Package(Nil),
  classes: List[ClassDeclTree] = Nil
) extends ImportErrors {

  override val importMap: ImportMap = this
  private  val shortToFull          = mutable.Map[String, String]()
  private  val fullToShort          = mutable.Map[String, String]()

  var extensionSymbols: List[ExtensionClassSymbol] = Nil

  private val javaObject = List("java", "lang", "Object")
  private val javaString = List("java", "lang", "String")
  private val TInt       = List("T", "lang", "Int")
  private val TLong      = List("T", "lang", "Long")
  private val TFloat     = List("T", "lang", "Float")
  private val TDouble    = List("T", "lang", "Double")
  private val TChar      = List("T", "lang", "Char")
  private val TBool      = List("T", "lang", "Bool")
  private val TLang      = List("T", "lang")

  private val DefaultImports = List[Import](
    RegularImport(javaObject),
    RegularImport(javaString),
    RegularImport(TInt),
    RegularImport(TLong),
    RegularImport(TFloat),
    RegularImport(TDouble),
    RegularImport(TChar),
    RegularImport(TBool),
    ExtensionImport(TLang, javaObject),
    ExtensionImport(TLang, javaString),
    ExtensionImport(TLang, TInt),
    ExtensionImport(TLang, TLong),
    ExtensionImport(TLang, TFloat),
    ExtensionImport(TLang, TDouble),
    ExtensionImport(TLang, TChar)
  )

  // Initialize
  {
    val ignoredImports = ctx.ignoredImports

    val defaultImportNames = DefaultImports.map(_.writtenName)
    ignoredImports
      .filter(!defaultImportNames.contains(_))
      .foreach(imp => report(DefaultImportDoesntExist(imp, NoPosition)))

    val defaultImports = DefaultImports.filter(imp => !ctx.ignoredImports.contains(imp.writtenName))
    defaultImports ++ imports foreach +=

    val packName = pack.name
    if (packName.nonEmpty) {
      classes.filterInstance[IDClassDeclTree] foreach { clazz =>
        val className = clazz.id.name
        this += (className, s"$packName::$className")
      }
    }
  }

  def getExtensionClasses(className: String): List[ExtensionClassSymbol] =
    extensionSymbols.filter { extSym =>
      val name = ExtensionDecl.stripExtension(extSym.name)
      name == className
    }

  def addExtensionClass(extensionClassSymbol: ExtensionClassSymbol): Unit = extensionSymbols ::= extensionClassSymbol

  def +=(tup: (String, String)): Unit = this += (tup._1, tup._2)
  def +=(short: String, full: String): this.type = {
    shortToFull += short -> full
    fullToShort += full -> short
    this
  }

  def +=(imp: Import): this.type = {
    imp match {
      case regImp: RegularImport            =>
        val fullName = regImp.name
        val shortName = regImp.shortName
        val templateImporter = new TemplateImporter(ctx)

        if (contains(shortName))
          report(ConflictingImport(regImp.writtenName, getFullName(shortName), regImp))
        else if (!(templateImporter.classExists(fullName) || ClassSymbolLocator.classExists(fullName)))
          report(CantResolveImport(regImp.writtenName, regImp))
        else
          this += (shortName, fullName)
      case extensionImport: ExtensionImport =>
        ClassSymbolLocator.findExtensionSymbol(extensionImport.name) match {
          case Some(e) => addExtensionClass(e)
          case None    => report(CantResolveExtensionsImport(extensionImport, extensionImport))
        }
      case _: WildCardImport                => // TODO: Support wild card imports.
    }
    this
  }

  def ++=(imps: ImportMap): this.type = {imps.imports foreach {this += _}; this}

  def getFullName(shortName: String): String = shortToFull.getOrElse(shortName, shortName)
  def getShortName(fullName: String): String = fullToShort.getOrElse(fullName, fullName)

  def replaceNames(str: String): String =
    fullToShort.foldLeft(str) { case (s, (full, short)) => s.replaceAll(full, short) }

  def contains(shortName: String): Boolean = shortToFull.contains(shortName)

  def entries: Iterator[(String, String)] = shortToFull.iterator

  override def toString: String = {
    shortToFull.map { case (short, full) => s"$short -> $full" }.mkString("\n")
  }


}