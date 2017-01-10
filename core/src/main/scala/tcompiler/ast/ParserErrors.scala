package tcompiler.ast

import tcompiler.ast.Trees.TypeTree
import tcompiler.imports.ImportMap
import tcompiler.lexer.{Token, TokenKind}
import tcompiler.utils.{Errors, Positioned}

/**
  * Created by Tim Lindeberg on 5/13/2016.
  */
trait ParserErrors extends Errors {

  override val ErrorPrefix = "P"
  override var importMap = new ImportMap(ctx)

  private def error(errorCode: Int, msg: String, pos: Positioned): Unit =
    ctx.reporter.error(ErrorPrefix, errorCode, msg, pos, importMap)

  //---------------------------------------------------------------------------------------
  //  Error messages
  //---------------------------------------------------------------------------------------

  protected def ErrorImplicitMethodOrOperator(pos: Positioned) =
    error(0, "Only constructors can be declared implicit.", pos)

  protected def ErrorStaticIndexingOperator(name: String, pos: Positioned) =
    error(1, s"Indexing operator '$name' cannot be declared static.", pos)

  protected def ErrorInvalidArrayDimension(size: Int, pos: Positioned) ={
    val maxArraySize = ASTBuilder.MaximumArraySize
    error(2, s"Invalid array dimension: '$size', '$maxArraySize' is the maximum dimension of an array.", pos)
  }

  //---------------------------------------------------------------------------------------
  //  Fatal messages
  //---------------------------------------------------------------------------------------

  protected def FatalExpectedIdAssignment(pos: Positioned) =
    fatal(1, "Expected identifier or array access on left side of assignment.", pos)

  protected def FatalWrongToken(currentToken: Token, kind: TokenKind, more: TokenKind*): Nothing =
    FatalWrongToken((kind :: more.toList).map(k => s"'$k'").mkString(" or "), currentToken.toString, currentToken)

  protected def FatalWrongToken(expected: String, found: String, pos: Positioned): Nothing =
    fatal(2, s"Expected $expected, found: '$found'.", pos)

  protected def FatalUnexpectedToken(currentToken: Token) =
    fatal(3, s"Unexpected token: '$currentToken'", currentToken)

}