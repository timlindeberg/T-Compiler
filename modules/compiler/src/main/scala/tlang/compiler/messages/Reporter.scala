package tlang
package compiler
package messages

import tlang.utils.Logging

trait Reporter {

  def report(error: CompilerMessage): Unit
  def clear(): Unit
  def terminateIfErrors(): Unit

  def isEmpty: Boolean = !hasErrors && !hasWarnings

  def hasErrors: Boolean
  def hasWarnings: Boolean

  def messages: CompilerMessages
}

case class DefaultReporter(messages: CompilerMessages = CompilerMessages()) extends Reporter with Logging {

  def report(message: CompilerMessage): Unit = {
    info"Reporting compiler message: $message"

    messages += message

    if (message.messageType == MessageType.Fatal)
      throwException()
  }

  def clear(): Unit = messages.clear()

  def terminateIfErrors(): Unit = {
    if (hasErrors) {
      info"Terminating compilation since there were ${ messages(MessageType.Error).length } errors"
      throwException()
    }
  }

  def hasErrors: Boolean = messages(MessageType.Error).nonEmpty
  def hasWarnings: Boolean = messages(MessageType.Warning).nonEmpty

  private def throwException(): Nothing = {
    val e = new CompilationException(messages.clone())
    clear()
    throw e
  }
}

case class VoidReporter() extends Reporter {

  private var _hasErrors = false
  private var _hasWarnings = false

  override def report(message: CompilerMessage): Unit = message match {
    case _: FatalMessage   => throw new CompilationException(null: CompilerMessages)
    case _: WarningMessage => _hasWarnings = true
    case _                 => _hasErrors = true
  }

  override def clear(): Unit = {
    _hasErrors = false
    _hasWarnings = false
  }
  override def terminateIfErrors(): Unit = {}

  override def hasErrors: Boolean = _hasErrors
  override def hasWarnings: Boolean = _hasWarnings

  override def messages: CompilerMessages = null
}
