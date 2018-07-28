package tlang.compiler.output

import tlang.compiler.messages._
import tlang.formatting.Formatter
import tlang.formatting.grid.{CenteredContent, Column, Grid, TruncatedColumn}
import tlang.formatting.textformatters.SyntaxHighlighter
import tlang.options.argument.MessageContextFlag
import tlang.utils.Extensions._
import tlang.utils.JSON.Json

object ErrorMessageOutput {
  val DefaultMessageTypes = List(MessageType.Warning, MessageType.Error)
}

case class ErrorMessageOutput(
  syntaxHighlighter: SyntaxHighlighter,
  compilerMessages: CompilerMessages,
  messageContextSize: Int = MessageContextFlag.defaultValue,
  messageTypes: List[MessageType] = ErrorMessageOutput.DefaultMessageTypes
)(implicit formatter: Formatter) extends Output {

  override def pretty: String = {
    val formatting = formatter.formatting
    import formatting._

    def format(messageType: MessageType): String = {
      val messages = compilerMessages(messageType)
      if (messages.isEmpty)
        return ""

      val grid = formatter.grid.header(header(messageType))
      messages.foreach { addToGrid(grid, _) }
      grid.render()
    }

    def header(messageType: MessageType): String = {
      val messages = compilerMessages(messageType)

      val n = messages.size
      if (n == 0)
        return ""

      val (was, appendix) = if (n == 1) ("was", "") else ("were", "s")

      val name = messageType.name.toLowerCase + appendix
      val color = messageType.color
      val num = color(n)
      if (compilerMessages.hitMax(messageType))
        s"${ Bold }There were more than $num$Bold $name, only showing the first $num$Reset."
      else
        s"${ Bold }There $was $num$Bold $name.$Reset"
    }

    def addToGrid(grid: Grid, message: CompilerMessage): Unit = {
      addMessage(message, grid, showTitle = true)
      message.notes foreach { addMessage(_, grid, showTitle = false) }
    }

    def addMessage(message: CompilerMessage, grid: Grid, showTitle: Boolean): Unit = {
      val messageInfo = MessageInfo(message, syntaxHighlighter, messageContextSize)

      grid.row()

      if (showTitle) {
        val color = message.messageType.color
        val title = s" ${ messageInfo.prefix.stripAnsi } "
        grid.content(CenteredContent(title, color, formatting.HorizontalThick))
      }

      val validPos = messageInfo.hasValidPosition
      if (validPos)
        grid.content(messageInfo.sourceDescription)

      grid.content(message.message)

      if (validPos) {
        grid.row(Column, TruncatedColumn)
        grid.contents(messageInfo.locationInSource)
      }
    }

    messageTypes.map(format).filter(_.nonEmpty).mkString(NL)
  }


  override def json: Json = {

    def format(messageType: MessageType): Json = {
      val messages = compilerMessages(messageType)
      val key = s"compilation${messageType}s"

      Json(key -> messages.map(formatMessage))
    }

    def formatMessage(message: CompilerMessage): Json = {
      val pos = message.pos
      Json(
        "start" -> Json(
          "line" -> pos.line,
          "col" -> pos.col
        ),
        "end" -> Json(
          "line" -> pos.lineEnd,
          "col" -> pos.colEnd
        ),
        "source" -> pos.sourceDescription,
        "code" -> message.code,
        "message" -> message.message,
        "notes" -> message.notes.map(formatMessage)
      )
    }

    messageTypes.map(format).reduce(_ ++ _).asInstanceOf[Json]
  }
}
