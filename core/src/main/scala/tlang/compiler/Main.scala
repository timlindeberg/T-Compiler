package tlang.compiler

import tlang.Constants._
import tlang.compiler.analyzer.{Flowing, Naming, Typing}
import tlang.compiler.ast.Parsing
import tlang.compiler.ast.Trees._
import tlang.compiler.code.{CodeGeneration, Lowering}
import tlang.compiler.error._
import tlang.compiler.imports.ClassPath
import tlang.compiler.lexer.Lexing
import tlang.compiler.modification.Templating
import tlang.compiler.options.{Flags, Options}
import tlang.utils.Extensions._
import tlang.utils.formatting._
import tlang.utils.formatting.grid.Alignment.Center
import tlang.utils.formatting.grid.Width.Percentage
import tlang.utils.formatting.grid.{Column, Grid}
import tlang.utils.{FileSource, ProgramExecutor, Source}
import tlang.{Constants, Context}

object Main extends MainErrors {

  import Flags._

  val FrontEnd: CompilerPhase[Source, CompilationUnit] =
    Lexing andThen Parsing andThen Templating andThen
      Naming andThen Typing andThen Flowing andThen Lowering


  val CompilerPhases = List(
    Lexing,
    Parsing,
    Templating,
    Naming,
    Typing,
    Flowing,
    Lowering,
    CodeGeneration
  )

  def main(args: Array[String]) {
    val options = Options(args)

    val formatting = options.formatting
    val formatter = Formatter(formatting)

    if (args.isEmpty) {
      print(printHelpInfo(formatter))
      sys.exit(1)
    }

    printHelp(formatter, options)

    if (!isValidTHomeDirectory(TDirectory))
      FatalInvalidTHomeDirectory(TDirectory, THome)

    if (options.files.isEmpty)
      FatalNoFilesGiven()


    val ctx = createContext(options, formatter)

    if (options(Verbose)) printFilesToCompile(ctx)

    val CUs = runFrontend(ctx)

    CodeGeneration.execute(ctx)(CUs)

    ctx.reporter.printWarnings()

    if (options(Verbose))
      printExecutionTimes(ctx)

    if (options(Exec))
      executePrograms(ctx, CUs)
  }

  private def runFrontend(ctx: Context): List[CompilationUnit] = {
    try {
      val sources = ctx.files.toList.map(FileSource(_))
      FrontEnd.execute(ctx)(sources)
    } catch {
      case e: CompilationException =>
        e.messages.printWarnings()
        e.messages.printErrors()
        sys.exit(1)
    }
  }

  private def createContext(options: Options, formatter: Formatter): Context = {

    val errorFormatter = ErrorFormatter(formatter, options(ErrorContext))
    val messages = ErrorMessages(errorFormatter, maxErrors = options(MaxErrors))
    val debugOutputFormatter = DebugOutputFormatter(formatter)
    Context(
      reporter = DefaultReporter(
        suppressWarnings = options(SuppressWarnings),
        warningIsError = options(WarningIsError),
        messages = messages,
        formatting = formatter.formatting
      ),
      formatter = formatter,
      debugOutputFormatter = debugOutputFormatter,
      files = options.files,
      classPath = ClassPath.Default ++ options.classPaths,
      outDirs = options.outDirectories,
      printCodePhase = options(PrintOutput),
      ignoredImports = options(IgnoreDefaultImports)
    )
  }

  private def printFilesToCompile(ctx: Context) = {
    val formatting = ctx.formatter.formatting
    import formatting._

    val numFiles = ctx.files.size
    val end = if (numFiles > 1) "files" else "file"
    val grid = Grid(ctx.formatter)
      .header(Bold("Compiling") + " " + Blue(numFiles) + " " + Bold(end))

    val fileNames = ctx.files.toList.map(f => formatFileName(f).stripSuffix(Constants.FileEnding))
    formatting.lineWidth match {
      case x if x in (0 to 59)  =>
        grid.row().allContent(List(fileNames))
      case x if x in (60 to 99) =>
        grid
          .row(Column(width = Percentage(0.5)), Column(width = Percentage(0.5)))
          .mapContent(fileNames.grouped(2).toList) {
            case f1 :: f2 :: Nil => (f1, f2)
            case f :: Nil        => (f, "")
            case _               => ("", "")
          }
      case x if x >= 100        =>
        grid
          .row(Column(width = Percentage(0.333)), Column(width = Percentage(0.333)), Column(width = Percentage(0.333)))
          .mapContent(fileNames.grouped(3).toList) {
            case f1 :: f2 :: f3 :: _ => (f1, f2, f3)
            case f1 :: f2 :: Nil     => (f1, f2, "")
            case f :: Nil            => (f, "", "")
            case _                   => ("", "", "")
          }
    }
    grid.print()
  }

  private def printExecutionTimes(ctx: Context) = {
    val formatting = ctx.formatter.formatting
    import formatting._

    val totalTime = ctx.executionTimes.values.sum

    Grid(ctx.formatter)
      .header(f"${ Bold }Compilation executed ${ Green("successfully") }$Bold in $Green$totalTime%.2f$Reset ${ Bold }seconds.$Reset")
      .row(2)
      .mapContent(CompilerPhases) { phase =>
        val time = ctx.executionTimes(phase)
        (Blue(phase.phaseName.capitalize), Green(f"$time%.2f$Reset") + " s")
      }
      .print()
  }

  private def versionInfo = s"T-Compiler $VersionNumber"

  private def printHelp(formatter: Formatter, options: Options) = {
    if (options(Version)) {
      print(versionInfo)
      sys.exit()
    }

    if (options(Phases)) {
      print(phaseInfo(formatter))
      sys.exit()
    }

    val args = options(Help)
    if (args.contains("")) {
      printHelpInfo(formatter)
      sys.exit()
    }

    args.foreach(Flag.get(_) ifDefined { flag =>
      printFlagInfo(flag, formatter)
    })

    if (args.nonEmpty) {
      sys.exit()
    }
  }

  private def printFlagInfo(flag: Flag, formatter: Formatter) = {
    val formatting = formatter.formatting
    Grid(formatter)
      .header(flag.flagName(formatting))
      .row()
      .content(flag.extendedDescription(formatter))
      .print()
  }

  private def printHelpInfo(formatter: Formatter) = {
    val formatting = formatter.formatting
    import formatting._

    val tcomp = Green("tcomp")
    val options = Blue("options")
    val source = Blue("source files")
    val optionsHeader = Bold(Magenta("Options"))

    Grid(formatter)
      .header(s"> $tcomp <$options> <$source> \n\n $optionsHeader")
      .row(2)
      .mapContent(Flag.All) { flag => (flag.flagName(formatting), flag.description(formatter)) }
      .toString
  }

  private def phaseInfo(formatter: Formatter) = {
    val formatting = formatter.formatting
    import formatting._

    Grid(formatter)
      .header(Bold(s"Phases of the T-Compiler"))
      .row(2)
      .mapContent(CompilerPhases) { phase => (Magenta(phase.phaseName.capitalize), phase.description(formatting)) }
      .toString
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
      "T",
      "T/lang",
      "T/lang/Object.t",
      "T/lang/String.t",
      "T/std"
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

  private def executePrograms(ctx: Context, cus: List[CompilationUnit]): Unit = {
    val formatting = ctx.formatter.formatting
    import formatting._

    val mainMethods = cus.flatMap(_.classes.flatMap(_.methods.filter(_.isMain)))
    if (mainMethods.isEmpty) {
      println("--exec failed, none of the given files contains a main method.")
      return
    }

    val grid = Grid(ctx.formatter)
      .header(Bold(if (mainMethods.size > 1) "Executing programs" else "Executing program"))

    val programExecutor = ProgramExecutor()
    cus.foreach { cu =>
      val file = cu.source.asInstanceOf[FileSource].file
      val output = ctx.formatter.syntaxHighlighter(programExecutor(ctx, file))
      grid
        .row(alignment = Center)
        .content(formatFileName(file))
        .row(2)
        .mapContent(output.split("\n").zipWithIndex) { case (line, i) => (Magenta(i + 1), line) }
    }
    grid.print()
  }

}
