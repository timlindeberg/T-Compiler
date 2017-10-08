package tlang.options.arguments

import tlang.formatting.Formatter
import tlang.messages.ErrorStringContext
import tlang.options.NumberFlag

case object MessageContextFlag extends NumberFlag {
  override val defaultValue = 2

  override val name      = "messagecontext"
  override val shortFlag = Some("c")


  protected override def verifyArgument(arg: String)(implicit errorContext: ErrorStringContext): Unit = {
    super.verifyArgument(arg)
    import errorContext.ErrorStringContext
    val num = arg.toInt
    if (num < -1) {
      error(err"$num is not a number of message context lines.")
    }
  }


  override def description(formatter: Formatter): String = {
    import formatter.formatting._
    s"""
       |Specify how many lines to display around an error position in error messages and warnings.
       |The default is '${ Blue(defaultValue) }'.
      """.stripMargin.trim
  }

}