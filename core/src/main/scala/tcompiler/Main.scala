package tcompiler

import tcompiler.analyzer.{FlowAnalysis, NameAnalysis, TypeChecking}
import tcompiler.ast.Trees._
import tcompiler.ast.{Parser, PrettyPrinter}
import tcompiler.code.{CodeGeneration, Desugaring}
import tcompiler.error.Boxes.Simple
import tcompiler.error.{CompilationException, DefaultReporter, Formatting, SyntaxHighlighter}
import tcompiler.lexer.Lexer
import tcompiler.modification.Templates
import tcompiler.utils._

import scala.concurrent.duration

object Main extends MainErrors {

  import Flags._

  val FileEnding           = ".kool"
  val VersionNumber        = "0.0.1"
  val THome                = "T_HOME"
  val JavaObject           = "java/lang/Object"
  val JavaString           = "java/lang/String"
  val TExtensionAnnotation = "kool/lang/$ExtensionMethod"


  lazy val TDirectory: String = {
    if (!sys.env.contains(THome))
      FatalCantFindTHome(THome)
    sys.env(THome)
  }

  val CompilerStages = List(
    Lexer,
    Parser,
    Templates,
    NameAnalysis,
    TypeChecking,
    FlowAnalysis,
    Desugaring,
    CodeGeneration
  )

  def main(args: Array[String]) {
    val options = Options(args)
    val useColor = options.boxType != Simple
    val colors = Colors(useColor)

    if (args.isEmpty) {
      printHelp(colors)
      sys.exit(1)
    }

    if (!isValidTHomeDirectory(TDirectory))
      FatalInvalidTHomeDirectory(TDirectory, THome)


    val formatting = error.Formatting(options.boxType, options(LineWidth), colors)
    if (options(Version)) {
      printVersion()
      sys.exit()
    }
    if (options(Help).nonEmpty) {
      printHelp(colors, options(Help))
      sys.exit()
    }
    if (options.files.isEmpty)
      FatalNoFilesGiven()

    ctx = createContext(options, formatting)

    if (options(Verbose))
      printFilesToCompile(ctx)

    val cus = runFrontend(ctx)

    val compilation = Desugaring andThen CodeGeneration
    compilation.run(ctx)(cus)

    if (ctx.reporter.hasWarnings)
      println(ctx.reporter.warningMessage)

    if (options(Verbose))
      printExecutionTimes(ctx)

    if (options(Exec))
      executeProgram(ctx, cus)
  }

  private def runFrontend(ctx: Context): List[CompilationUnit] = {
    val frontEnd = Lexer andThen Parser andThen Templates andThen
      NameAnalysis andThen TypeChecking andThen FlowAnalysis

    val cus = try {
      frontEnd.run(ctx)(ctx.files)
    } catch {
      case e: CompilationException =>
        println(e.getMessage)
        sys.exit(1)
    }

    cus
  }

  private def createContext(options: Options, formatting: Formatting): Context =
    Context(
      reporter = new DefaultReporter(
        suppressWarnings = options(SuppressWarnings),
        warningIsError = options(WarningIsError),
        maxErrors = options(MaxErrors),
        errorContext = options(ErrorContext),
        formatting = formatting
      ),
      files = options.files,
      classPaths = options.classPaths,
      outDirs = options.outDirectories,
      printCodeStages = options(PrintOutput),
      printInfo = options(Verbose),
      ignoredImports = options(IgnoreDefaultImports),
      formatting = formatting,
      printer = PrettyPrinter(formatting.colors)
    )

  private def printFilesToCompile(ctx: Context) = {
    import ctx.formatting._
    import ctx.formatting.colors._
    val numFiles = ctx.files.size
    val files = ctx.files.map { f =>
      val name = f.getName.dropRight(Main.FileEnding.length)
      val full = s"${Magenta(name)}${Main.FileEnding}"
      s"$full"
    }.mkString("\n")
    val end = if (numFiles > 1) "files" else "file"
    val header = Bold("Compiling") + " " + Magenta(numFiles) + " " + Bold(end)

    print(makeBox(header, List(files)))
  }

  private def printExecutionTimes(ctx: Context) = {
    import ctx.formatting._
    import ctx.formatting.colors._
    val totalTime = ctx.executionTimes.values.sum
    val individualTimes = CompilerStages.map { stage =>
      val name = Blue(stage.stageName.capitalize)
      val time = ctx.executionTimes(stage)
      val t = Green(f"$time%.2f$Reset")
      f"$name%-25s $t s"
    }.mkString("\n")

    val header =
      f"${Bold}Compilation executed ${Green("successfully")}$Bold in $Green$totalTime%.2f$Reset ${Bold}seconds.$Reset"
    print(makeBox(header, List(individualTimes)))
  }

  private def printVersion() = println(s"T-Compiler $VersionNumber")

  private def printHelp(colors: Colors, args: Set[String] = Set("")) = args foreach { arg =>
    import colors._
    val message = arg match {
      case "stages" =>
        val stages = CompilerStages.map(stage => s"   <$Blue${stage.stageName.capitalize}$Reset>").mkString("\n")
        s"""|The compiler stages are executed in the following order:
            |
            |$stages
            |"""
      case ""       =>
        val flags = Flag.All.map(_.format(colors)).mkString
        s"""|Usage: tcomp <options> <source files>
            |Options:
            |
            |$flags
            |"""
      case _        => ???
    }
    print(message.stripMargin)
  }

  private def isValidTHomeDirectory(path: String): Boolean = {
    // TODO: Make this properly check that the directory is valid
    /*
    def listFiles(f: File): Array[File] = {
      val these = f.listFiles
      if (these == null)
        return Array[File]()
      these ++ these.filter(_.isDirectory).flatMap(listFiles)
    }

    val files = listFiles(new File(path))
    val neededFiles = List(
      "kool",
      "kool/lang",
      "kool/lang/Object.kool",
      "kool/lang/String.kool",
      "kool/std"
    )
    val fileMap = mutable.Map() ++ neededFiles.map((_, false))
    val filePaths = files.map(_.getAbsolutePath.drop(path.length + 1).replaceAll("\\\\", "/"))
    for (f <- filePaths)
      fileMap(f) = true

    if (fileMap.exists(!_._2))
      return false
    */
    true

  }

  private def executeProgram(ctx: Context, cus: List[CompilationUnit]): Unit = {
    import ctx.formatting._
    import ctx.formatting.colors._

    val mainMethods = cus.flatMap(_.classes.flatMap(_.methods.filter(_.isMain)))
    if (mainMethods.isEmpty) {
      println("--exec failed, none of the given files contains a main method.")
      return
    }

    val header = if (mainMethods.size > 1) "Executing programs" else "Executing program"


    val programExecutor = ProgramExecutor(duration.Duration(1, "min"))
    val syntaxHighlighter = SyntaxHighlighter(ctx.formatting.colors)
    val outputBlocks = cus.flatMap { cu =>
      val file = cu.file.get
      val output = syntaxHighlighter(programExecutor(ctx, file).get)
      val name = file.getName.dropRight(Main.FileEnding.length)
      val mainName = s"$Bold${Magenta(name)}${Bold(Main.FileEnding)}"

      List(center(mainName), output)
    }

    print(makeBox(Bold(header), outputBlocks))
  }

}