package tlang.testutils

import better.files.File
import org.scalatest.{FlatSpec, Matchers}
import tlang.formatting.Colors.ColorScheme
import tlang.formatting.Colors.ColorScheme.DefaultColorScheme
import tlang.formatting._
import tlang.formatting.textformatters.{StackTraceHighlighter, SyntaxHighlighter, Truncator, WordWrapper}
import tlang.testutils.snapshot.SnapshotTesting
import tlang.utils.Extensions._

trait UnitSpec extends FlatSpec with Matchers with AnsiMatchers with MockitoSugar with SnapshotTesting {

  def mockedWordWrapperReturningSplitLines: WordWrapper = {
    mock[WordWrapper] use { wordWrapper =>
      wordWrapper.apply(*, *) answers { _.getArgument[String](0).split("\r?\n", -1).toList }
      wordWrapper.wrapAnsiFormatting(*) forwardsArg 0
    }
  }


  def memoryFile(content: String = ""): (StringBuilder, File) = {
    val buffer = new StringBuilder
    val file = mock[File]
    // Save the data to a local stringBuilder instead
    file.write(*)(*, *) answers { invocation =>
      buffer.clear
      buffer ++= invocation.getArgument(0)
      file
    }

    file.appendLine(*)(*) answers { invocation =>
      buffer ++= invocation.getArgument(0)
      buffer ++= NL
      file
    }

    file.exists returns true
    file.lineIterator returns content.lines

    (buffer, file)
  }


  def createMockFormatter(
    width: Int = 80,
    useColor: Boolean = true,
    asciiOnly: Boolean = true,
    colorScheme: ColorScheme = DefaultColorScheme,
    formatting: Option[Formatting] = None,
    wordWrapper: WordWrapper = mock[WordWrapper],
    truncator: Truncator = mock[Truncator],
    syntaxHighlighter: SyntaxHighlighter = mock[SyntaxHighlighter],
    stackTraceHighlighter: StackTraceHighlighter = mock[StackTraceHighlighter]
  ): Formatter = {

    Formatter(
      formatting = formatting.getOrElse(Formatting(width, colorScheme, useColor, asciiOnly)),
      wordWrapper,
      truncator,
      syntaxHighlighter,
      stackTraceHighlighter
    )
  }

}
