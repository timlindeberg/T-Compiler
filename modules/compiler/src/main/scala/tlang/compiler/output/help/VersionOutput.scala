package tlang.compiler.output.help

import tlang.Constants
import tlang.compiler.output.Output
import tlang.formatting.Formatter
import tlang.utils.JSON.Json

case class VersionOutput()(implicit formatter: Formatter) extends Output {
  override def pretty: String = {
    import formatter.formatting._
    s"${Green(Constants.CommandName) } version ${ Blue(Constants.Version) }"
  }

  override def json: Json = Json("version" -> Constants.Version)
}