package tlang
package compiler
package argument

import tlang.formatting.{ErrorStringContext, Formatter}
import tlang.options.ArgumentFlag
import tlang.utils.{Executor, ParallellExecutor, SingleThreadExecutor}

case object ThreadsFlag extends ArgumentFlag[Executor] {
  override val name = "threads"
  override val shortFlag = Some("j")
  override val argDescription = "num"

  override def description(implicit formatter: Formatter): String =
    "Specifies how many threads should be used. By default compilation is single threaded."

  override def extendedDescription(implicit formatter: Formatter): String =
    s"""
       |Specifies how many threads should be used. By default compilation is single threaded.
       |
       |Give ${ highlight("0") } to use the available number of processors.
       |
       |Multiple threads will be used when possible. In general a compiler stage will execute in parallel for each given file except for when there are dependencies between compilation units.
       |During code generation each class file is generated in parallel.
      """

  override def parseValue(args: Set[String]): Executor = {
    if (args.isEmpty)
      return SingleThreadExecutor

    var numThreads = args.map { _.toInt }.max
    if (numThreads == 0) numThreads = Runtime.getRuntime.availableProcessors

    if (numThreads == 1) SingleThreadExecutor else ParallellExecutor(numThreads)
  }

  protected override def verify(numThreads: String)(implicit errorContext: ErrorStringContext): Unit = {
    import errorContext.ErrorStringContext
    if (!numThreads.isNumber)
      error(err"The argument $numThreads is not a valid number.")

    val num = numThreads.toInt

    if (num < 0)
      error(err"Invalid number of threads: $num. Number has to be 0 or larger.")
  }
}
