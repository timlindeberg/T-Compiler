package tlang.formatting.grid

import tlang.utils.Extensions._

import scala.collection.mutable.ListBuffer

object ColumnDefaults {
  val Width            = tlang.formatting.grid.Width.Auto
  val OverflowHandling = tlang.formatting.grid.OverflowHandling.Wrap
  val Alignment        = tlang.formatting.grid.Alignment.Left
}

object Column extends Column(ColumnDefaults.Width, ColumnDefaults.Alignment, ColumnDefaults.OverflowHandling)
object CenteredColumn extends Column(ColumnDefaults.Width, Alignment.Center, ColumnDefaults.OverflowHandling)
object TruncatedColumn extends Column(ColumnDefaults.Width, ColumnDefaults.Alignment, OverflowHandling.Truncate)

case class Column(
  width: Width = ColumnDefaults.Width,
  alignment: Alignment = ColumnDefaults.Alignment,
  overflowHandling: OverflowHandling = ColumnDefaults.OverflowHandling) {

  private[grid] val lines: ListBuffer[String] = ListBuffer()
  private       var _maxWidth                 = 0

  def maxWidth: Int = _maxWidth
  def addLine(newLine: String): Unit = {
    _maxWidth = Math.max(_maxWidth, newLine.visibleCharacters)
    lines += newLine
  }

  def content: String = lines.mkString("\n")

}

trait Width
trait FixedWidth extends Width {
  def apply(maxWidth: Int): Int
}

object Width {
  case object Auto extends Width
  case class Fixed(width: Int) extends FixedWidth {
    def apply(maxWidth: Int): Int = width
  }
  case class Percentage(widthPercentage: Double) extends FixedWidth {
    if (widthPercentage < 0.0 || widthPercentage > 1.0)
      throw new IllegalArgumentException("Percentage width should be between 0 and 1")

    def apply(maxWidth: Int): Int = (widthPercentage * maxWidth).toInt
  }
}

trait Alignment {
  def apply(text: String, width: Int, fill: Char = ' '): String = {
    if (width < 1)
      throw new IllegalArgumentException(s"Cannot align text within a space smaller than 1: $width")

    val textWidth = text.visibleCharacters
    if (textWidth > width) {
      throw new IllegalArgumentException(s"Cannot align text '$text' in the given space: $textWidth > $width")
    }

    align(text, width - textWidth, fill)
  }

  protected def align(text: String, space: Int, fill: Char): String

}

object Alignment {
  case object Left extends Alignment {
    override def align(text: String, space: Int, fill: Char): String = {
      text + s"$fill" * space
    }
  }
  case object Right extends Alignment {
    override def align(text: String, space: Int, fill: Char): String = {
      s"$fill" * space + text
    }
  }
  case object Center extends Alignment {
    override def align(text: String, space: Int, fill: Char): String = {
      val halfSpace = s"$fill" * (space / 2)
      val left = halfSpace
      val right = if (space % 2 == 0) halfSpace else halfSpace + fill
      left + text + right
    }
  }
}

trait OverflowHandling
object OverflowHandling {

  case object Except extends OverflowHandling
  case object Wrap extends OverflowHandling
  case object Truncate extends OverflowHandling
}
