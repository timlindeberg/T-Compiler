package tcompiler.lexer

import java.io.File

import tcompiler.lexer.Tokens.BAD
import tcompiler.utils.{Errors, Position, Positioned}

/**
  * Created by Tim Lindeberg on 5/13/2016.
  */
trait LexerErrors extends Errors {

  override val ErrorPrefix = "L"
  val file: File
  var line: Int
  var column: Int

  def error(errorCode: Int, msg: String, startPos: Positioned): Unit = {
    val file = startPos.file
    val start = Position.encode(startPos.line, startPos.col)
    val end = Position.encode(startPos.line + 1, 1)
    val bad = new Token(BAD).setPos(file, start , end)
    ctx.reporter.error(ErrorPrefix, errorCode, msg, bad)
  }

  protected def error(errorCode: Int, msg: String, colOffset: Int): Unit = {
    val bad = new Token(BAD).setPos(file, Position.encode(line, column), Position.encode(line, column + colOffset))
    ctx.reporter.error(ErrorPrefix, errorCode, msg, bad)
  }


  //---------------------------------------------------------------------------------------
  //  Error messages
  //---------------------------------------------------------------------------------------

  protected def ErrorInvalidCharacter(c: Char) =
    error(0, s"Invalid character: '$c'.", 1)

  protected def ErrorInvalidIdentifier(c: Char, length: Int) =
    error(1, s"Invalid character in identifier: '$c'.", length)

  protected def ErrorUnclosedMultilineString(startPos: Positioned) =
    error(2, "Unclosed multiline string literal.", startPos)

  protected def ErrorEmptyCharLiteral() =
    error(3, "Empty character literal.", 2)

  protected def ErrorInvalidEscapeSequence(length: Int) =
    error(4, "Invalid escape sequence.", length)

  protected def ErrorInvalidCharLiteral(length: Int) =
    error(5, "Invalid character literal.", length)

  protected def ErrorInvalidUnicode(length: Int) =
    error(6, "Invalid unicode escape sequence.", length)

  protected def ErrorUnclosedCharLiteral(length: Int) =
    error(7, "Unclosed character literal.", length)

  protected def ErrorUnclosedStringLiteral(startPos: Positioned) =
    error(8, "Unclosed string literal.", startPos)

  protected def ErrorNumberTooLarge(length: Int) =
    error(9, "Number is too large to fit in datatype.", length)

  protected def ErrorInvalidNumber(length: Int, rest: List[Char]) =
    error(10, "Invalid number.", length + rest.indexWhere(_.isWhitespace) + 1)

  protected def ErrorInvalidFloat(length: Int, rest: List[Char]) =
    error(11, "Invalid floating point number.", length + rest.indexWhere(_.isWhitespace) + 1)

}