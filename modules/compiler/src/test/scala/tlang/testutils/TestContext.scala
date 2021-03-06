package tlang
package testutils

import better.files.File
import tlang.Constants
import tlang.compiler.Context
import tlang.compiler.imports.ClassPath
import tlang.compiler.messages.DefaultReporter
import tlang.compiler.output.{JSONOutputHandler, PrettyOutputHandler}
import tlang.compiler.utils.TLangSyntaxHighlighter
import tlang.formatting.Formatter
import tlang.formatting.textformatters.{StackTraceHighlighter, SyntaxHighlighter}
import tlang.testutils.TestConstants.{PrintCodePhases, PrintJSON, Resources, TestOutputDirectory}

trait TestContext {

  val TestContext: Context = testContext(None)(TestConstants.TestFormatter)
  implicit val SyntaxHighlighter: SyntaxHighlighter = TLangSyntaxHighlighter()(TestConstants.TestFormatter)
  implicit val stackTraceHighlighter: StackTraceHighlighter = StackTraceHighlighter()(TestContext.formatter)

  def testContext(file: Option[File])(implicit formatter: Formatter): Context = {
    val outDir = file match {
      case Some(f) =>
        val resourceDir = File(Resources)
        val mainName = f.pathAsString.stripPrefix(resourceDir.pathAsString).stripSuffix(Constants.FileEnding)
        File(s"$TestOutputDirectory/$mainName/")
      case None    => File(".")
    }

    val outputHandler = if (PrintJSON) JSONOutputHandler() else PrettyOutputHandler()
    Context(
      reporter = DefaultReporter(),
      output = outputHandler,
      outDirs = Set(outDir),
      classPath = ClassPath.Default,
      printCodePhase = PrintCodePhases
    )
  }
}
