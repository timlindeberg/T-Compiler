package tlang
package options
package argument

import tlang.formatting.Formatter

case object MaxErrorsFlag extends NumberFlag {
  override val defaultValue = 100

  override val name: String = "maxerrors"

  override def description(implicit formatter: Formatter): String =
    s"""
       |Specify the maximum number of errors to report. The default is ${ highlight(defaultValue) }.
       |Enter a negative number to show all errors.
      """
}
