package tlang
package compiler
package execution

import tlang.compiler.argument.{VerboseFlag, WatchFlag}
import tlang.compiler.output.{ExecutionTimeOutput, InternalErrorOutput, InterruptedOutput, SuccessOutput}
import tlang.formatting.textformatters.StackTraceHighlighter
import tlang.utils.{InterruptionHandler, Logging}

case class Executor(ctx: Context, interruptionHandler: InterruptionHandler)
  (implicit stackTraceHighlighter: StackTraceHighlighter) extends Logging {

  import ctx.{formatter, options}

  interruptionHandler.setHandler(onInterrupt _)

  def execute[T](execute: => T): Option[T] = {
    try {
      Some(handleInternalErrors(execute))
    } catch {
      case ExitException(code) =>
        printExecutionTimes(success = false)
        if (!options(WatchFlag))
          exit(code)
        None
    }
  }

  def exit(code: Int): Nothing = {
    ctx.output += SuccessOutput(code == 0)
    ctx.output.flush()
    sys.runtime.halt(code)

    // So that exit can have Nothing as return type
    throw new RuntimeException("")
  }

  private def onInterrupt(): Unit = {
    ctx.output += InterruptedOutput()
    exit(0)
  }

  private def handleInternalErrors[T](execute: => T): T = {
    try {
      execute
    } catch {
      case e: ExitException => throw e
      case error: Throwable =>
        error"Execution error occurred: ${ error.stackTrace }"
        ctx.output += InternalErrorOutput(error)
        throw ExitException(1)
    }
  }

  private def printExecutionTimes(success: Boolean): Unit = {
    if (options(VerboseFlag))
      ctx.output += ExecutionTimeOutput(ctx.executionTimes.toMap, success)
  }
}
