package tlang
package testsuites

import org.scalatest.Suites
import tlang.compiler.CompilerSuite
import tlang.compiler.messages.MessagesSuite
import tlang.formatting.FormattingSuite
import tlang.options.OptionsSuite
import tlang.repl.ReplSuite
import tlang.utils.UtilsSuite

class UnitTestSuite extends Suites(
  new CompilerSuite,
  new ReplSuite,
  new FormattingSuite,
  new MessagesSuite,
  new OptionsSuite,
  new UtilsSuite
) {
  override def suiteName: String = "Unit Tests"
}