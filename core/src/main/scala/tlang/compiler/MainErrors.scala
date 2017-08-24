package tlang.compiler

import tlang.compiler.error.AlternativeSuggestor
import tlang.options.Arguments.FlagArgument

trait MainErrors {

  val nameSuggestor = new AlternativeSuggestor

  private def fatal(message: String) = {
    println(message)
    sys.exit(1)
  }

  //---------------------------------------------------------------------------------------
  // Errors
  //---------------------------------------------------------------------------------------

  protected def FatalCannotFindFile(fileName: String): Nothing =
    fatal(s"Cannot find file '$fileName'.")

  protected def FatalNoFilesGiven(): Nothing =
    fatal(s"No files given.")

  protected def FatalInvalidOutputDirectory(outDir: String): Nothing =
    fatal(s"Invalid output directory: '$outDir'.")

  protected def FatalOutputDirectoryCouldNotBeCreated(outDir: String): Nothing =
    fatal(s"Output directory '$outDir' does not exist and could not be created.")

  protected def FatalInvalidClassPath(classPath: String): Nothing =
    fatal(s"Invalid output class path: '$classPath'.")

  protected def FatalInvalidTHomeDirectory(path: String, tHome: String): Nothing =
    fatal(s"'$path' is not a valid $tHome directory.")

  protected def FatalGivenDirectoryContainsNoTFiles(path: String): Nothing =
    fatal(s"The given directory '$path' does not contain any T-files.")

  protected def FatalInvalidFlag(flag: String, alternatives: List[String]): Nothing =
    fatal(s"'$flag' is not a valid flag.${ nameSuggestor(flag, alternatives) } Type --help to see a list of valid commands.")

  protected def FatalInvalidJsonArgument(flag: FlagArgument[_], rest: String): Nothing =
    fatal(s"Input following JSON flag '${ flag.flag }' is not valid JSON: '$rest'.")

  protected def FatalGivenFileIsNotTFile(path: String): Nothing =
    fatal(s"The given file '$path' is not a T-file.")

  protected def FatalInvalidColorSchemeKey(key: String, alternatives: List[String]): Nothing =
    fatal(s"'$key' is not a valid part of color scheme.${ nameSuggestor(key, alternatives) }")

  protected def FatalInvalidColorSchemeArg(arg: String, alternatives: List[String]): Nothing =
    fatal(s"'$arg' is not a valid color.${ nameSuggestor(arg, alternatives) }")

  protected def FatalInvalidArgToFlag(flag: FlagArgument[_], arg: String, alternatives: List[String]): Nothing =
    fatal(s"'$arg' is not a valid argument to flag '--${ flag.flag }'.${ nameSuggestor(arg, alternatives) }")

  protected def FatalUnrecognizedArgument(arg: String): Nothing =
    fatal(s"'$arg' is not a valid argument.")

}
