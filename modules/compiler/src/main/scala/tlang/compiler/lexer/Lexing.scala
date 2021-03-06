package tlang
package compiler
package lexer

import java.math.BigInteger

import sourcecode.{Enclosing, Line}
import tlang.compiler.lexer.Tokens._
import tlang.compiler.messages.Reporter
import tlang.compiler.output.Output
import tlang.compiler.output.debug.TokenOutput
import tlang.formatting.{ErrorStringContext, Formatter}
import tlang.utils.{Logging, Position, Source}

import scala.annotation.tailrec

object Lexing extends CompilerPhase[Source, List[Token]] with Logging {

  val MaximumStringSize = 65535

  override protected def run(ctx: Context)(inputs: List[Source]): List[List[Token]] = {
    import ctx.formatter
    ctx.executor.map(inputs) { source =>
      info"Lexing ${ source.mainName }"
      val errorStringContext = ErrorStringContext()
      val lexer = Lexer(ctx.reporter, errorStringContext)
      lexer(source)
    }
  }

  override def description(implicit formatter: Formatter): String =
    "Lexes the input and produces tokens."

  override def debugOutput(output: List[List[Token]])(implicit formatter: Formatter): Output =
    TokenOutput(phaseName, output)
}

case class Lexer(override val reporter: Reporter, override val errorStringContext: ErrorStringContext) extends LexingErrors with Logging {

  protected override var line = 1
  protected override var column = 1
  protected override var source: Source = _

  def apply(source: Source): List[Token] = {

    @tailrec def readTokens(chars: List[Char], tokens: List[Token]): List[Token] = chars match {
      case ('\r' :: '\n' :: _) | ('\n' :: _) =>
        val (newToken, r) = parseNewline(chars)
        readTokens(r, newToken :: tokens)
      case (c :: _) if c.isWhitespace        =>
        val (tabs, rest) = parseTabs(chars)
        readTokens(rest, tabs ::: tokens)
      case '/' :: '/' :: r                   =>
        val (token, tail) = lineComment(r)
        readTokens(tail, token :: tokens)
      case '/' :: '*' :: r                   =>
        val (token, tail) = blockComment(r)
        readTokens(tail, token :: tokens)
      // Prioritise longer tokens
      case c1 :: c2 :: c3 :: r if tokenExists(s"$c1$c2$c3") =>
        readTokens(r, createToken(NonKeywords(s"$c1$c2$c3"), 3) :: tokens)
      case c1 :: c2 :: r if tokenExists(s"$c1$c2")          =>
        readTokens(r, createToken(NonKeywords(s"$c1$c2"), 2) :: tokens)
      case c :: r if tokenExists(s"$c")                     =>
        readTokens(r, createToken(NonKeywords(s"$c"), 1) :: tokens)
      case c :: _ if c.isLetter || c == '_'                 =>
        val (token, tail) = getIdentifierOrKeyword(chars)
        readTokens(tail, token :: tokens)
      case '\'' :: r                                        =>
        val (token, tail) = getCharLiteral(r)
        readTokens(tail, token :: tokens)
      case '"' :: r                                         =>
        val (token, tail) = getStringLiteral(r)
        readTokens(tail, token :: tokens)
      case '`' :: r                                         =>
        val (token, tail) = getMultiLineStringLiteral(r)
        readTokens(tail, token :: tokens)
      case '0' :: 'x' :: r                                  =>
        val (token, tail) = getHexadecimalLiteral(r)
        readTokens(tail, token :: tokens)
      case '0' :: 'b' :: r                                  =>
        val (token, tail) = getBinaryLiteral(r)
        readTokens(tail, token :: tokens)
      case c :: _ if c.isDigit                              =>
        val (token, tail) = getNumberLiteral(chars)
        readTokens(tail, token :: tokens)
      case Nil                                              =>
        val eof = if (tokens.nonEmpty && tokens.head.kind != DEDENT && tokens.head.kind != NEWLINE)
          createToken(EOF, 0) :: createToken(NEWLINE, 0) :: Nil
        else
          createToken(EOF, 0) :: Nil

        eof ::: tokens
      case c :: _                                           =>
        val (token, tail) = endInvalidToken(chars, 0, isEndingChar, length => report(InvalidIdentifier(c, length)))
        readTokens(tail, token :: tokens)
    }

    this.source = source
    val characters = source.text.toList
    val res = readTokens(characters, Nil).reverse

    line = 1
    column = 1
    calculateIndentationTokens(res).reverse
  }

  private def parseNewline(chars: List[Char]): (Token, List[Char]) = {
    val (newlinesParsed, rest) = parseNewlines(chars)
    val newlineToken = new Token(NEWLINE).setPos(source, line, column, line + newlinesParsed, 1)
    column = 1
    line += newlinesParsed

    (newlineToken, rest)
  }

  private def parseNewlines(chars: List[Char]): (Int, List[Char]) = {
    @tailrec def numNewlines(chars: List[Char], numChars: Int): (Int, List[Char]) = chars match {
      case '\r' :: '\n' :: rest => numNewlines(rest, numChars + 1)
      case '\n' :: rest         => numNewlines(rest, numChars + 1)
      case _                    => (numChars, chars)
    }

    numNewlines(chars, 0)
  }

  private def parseTabs(chars: List[Char]): (List[Token], List[Char]) = {
    var foundSpace = false
    var mixedTabsAndSpaces = false

    @tailrec def parseTabs(chars: List[Char], tokens: List[Token]): (List[Token], List[Char]) = chars match {
      case '\t' :: rest =>
        if (foundSpace)
          mixedTabsAndSpaces = true
        parseTabs(rest, createToken(TAB, 1) :: tokens)
      case ' ' :: rest  =>
        foundSpace = true
        column += 1
        parseTabs(rest, tokens)
      case _            => (tokens, chars)
    }

    parseTabs(chars, Nil) use { case (tokens, _) =>
      if (mixedTabsAndSpaces)
        report(IndentationMixesTabsAndSpaces(tokens.last, tokens.head))
    }
  }

  private def calculateIndentationTokens(tokens: List[Token]): List[Token] = {

    def createDedentTokens(tabs: List[Token], rest: List[Token]): List[Token] = {
      val dedentToken = new Token(DEDENT)
      if (tabs.nonEmpty) {
        dedentToken.setPos(tabs.last, tabs.head)
      } else {
        val nextPos = rest.head
        dedentToken.setPos(nextPos.source, nextPos.line, nextPos.col, nextPos.line, nextPos.col)
      }
      val newlineToken = new Token(NEWLINE)
      List(newlineToken, dedentToken)
    }

    @tailrec def untilNewLine(rest: List[Token], tokens: List[Token], tabs: List[Token]): (List[Token], List[Token]) = {
      def checkTabs(): Unit = {
        if (tabs.nonEmpty) {
          report(TabsNonIndentation(Position(tabs.last, tabs.head)))
        }
      }

      rest match {
        case (tab@Token(TAB)) :: r       => untilNewLine(r, tokens, tab :: tabs)
        case (token@Token(NEWLINE)) :: r =>
          checkTabs()
          (token :: tokens, r)
        case token :: r                  =>
          checkTabs()
          untilNewLine(r, token :: tokens, Nil)
        case Nil                         =>
          checkTabs()
          (tokens, Nil)
      }
    }

    @tailrec def addIndentTokens(rest: List[Token], tokens: List[Token], indent: Int, tabs: List[Token]): List[Token] = rest match {
      case Nil                            => tokens
      case (t@Token(TAB)) :: r            => addIndentTokens(r, tokens, indent, t :: tabs)
      case (t@Token(NEWLINE)) :: r        => addIndentTokens(r, t :: tokens, indent, Nil)
      case (t@Token(COMMENTLITKIND)) :: r => addIndentTokens(r, t :: tokens, indent, tabs)
      case list                           =>
        val tabSize = tabs.size
        val newIndent = tabSize
        val difference = newIndent - indent
        if (difference > 1) {
          val pos = Position(tabs.last, tabs.head)
          report(IndentationTooLarge(indent, newIndent, pos))
        }

        val indentTokens = if (difference == 1) {
          val indentToken = new Token(INDENT).setPos(tabs.last, tabs.head)
          indentToken :: Nil
        } else if (difference < 0) {
          val dedentTokens = createDedentTokens(tabs, list)
          List.fill(-difference)(dedentTokens).flatten
        } else {
          Nil
        }

        val (lineTokens, rest) = untilNewLine(list, indentTokens ::: tokens, Nil)

        addIndentTokens(rest, lineTokens, newIndent, Nil)
    }

    addIndentTokens(tokens, Nil, 0, Nil)
  }

  private def tokenExists(str: String): Boolean = NonKeywords.contains(str)

  private def getIdentifierOrKeyword(chars: List[Char]): (Token, List[Char]) = {

    def createIdentifierOrKeyWord(s: String): Token = KeywordMap.get(s) match {
      case Some(keyword) => createToken(keyword, s.length)
      case None          => createIdToken(s, s.length)
    }

    @tailrec def getIdentifierOrKeyword(chars: List[Char], s: StringBuilder, parsed: Int): (Token, List[Char]) = {
      def validChar(c: Char) = c.isLetter || c.isDigit || c == '_'

      chars match {
        case c :: r if validChar(c)    => getIdentifierOrKeyword(r, s + c, parsed + 1)
        case c :: _ if isEndingChar(c) => (createIdentifierOrKeyWord(s.toString), chars)
        case c :: r                    =>
          endInvalidToken(r, parsed, isEndingChar, length => report(InvalidIdentifier(c, length)))
        case Nil                       => (createIdentifierOrKeyWord(s.toString), chars)
      }
    }

    getIdentifierOrKeyword(chars, new StringBuilder, 1)
  }

  private def getCharLiteral(chars: List[Char]): (Token, List[Char]) = {

    @tailrec def toEnd(charList: List[Char], parsed: Int): (Token, List[Char]) =
      charList match {
        case Nil               =>
          report(UnclosedCharLiteral(parsed))
          (createToken(BAD, parsed), Nil)
        case '\'' :: r         =>
          report(InvalidCharLiteral(parsed))
          (createToken(BAD, parsed), r)
        case '\r' :: '\n' :: r =>
          line += 1
          column = 0
          toEnd(r, parsed + 2)
        case '\n' :: r         =>
          line += 1
          column = 0
          toEnd(r, parsed + 1)
        case _ :: r            =>
          toEnd(r, parsed + 1)
      }

    chars match {
      case '\'' :: r =>
        report(EmptyCharLiteral())
        (createToken(BAD, 2), r)

      // Unicodes
      case '\\' :: 'u' :: r => r match {
        case c1 :: c2 :: c3 :: c4 :: '\'' :: r if areHexDigits(c1, c2, c3, c4) =>
          val unicodeNumber = Integer.parseInt("" + c1 + c2 + c3 + c4, 16)
          (createToken(unicodeNumber.toChar, 8), r)
        case _ :: _ :: _ :: _ :: '\'' :: r                                     =>
          report(InvalidUnicode(8))
          (createToken(BAD, 8), r)
        case _ :: _ :: _ :: '\'' :: r                                          =>
          report(InvalidUnicode(7))
          (createToken(BAD, 7), r)
        case _ :: _ :: '\'' :: r                                               =>
          report(InvalidUnicode(6))
          (createToken(BAD, 6), r)
        case _ :: '\'' :: r                                                    =>
          report(InvalidUnicode(5))
          (createToken(BAD, 5), r)
        case '\'' :: r                                                         =>
          report(InvalidUnicode(4))
          (createToken(BAD, 4), r)
        case r                                                                 =>
          toEnd(r, 3)
      }

      // Escaped characters
      case '\\' :: c :: '\'' :: r => c match {
        case 't'  => (createToken('\t', 4), r)
        case 'b'  => (createToken('\b', 4), r)
        case 'n'  => (createToken('\n', 4), r)
        case 'r'  => (createToken('\r', 4), r)
        case 'f'  => (createToken('\f', 4), r)
        case '0'  => (createToken('\u0000', 4), r)
        case '\'' => (createToken('\'', 4), r)
        case '\\' => (createToken('\\', 4), r)
        case _    =>
          report(InvalidEscapeSequence(4))
          (createToken(BAD, 4), r)
      }
      case '\\' :: '\'' :: r      =>
        report(InvalidCharLiteral(3))
        (createToken(BAD, 4), r)
      case c :: '\'' :: r         =>
        (createToken(c, 3), r)
      case r                      =>
        toEnd(r, 1)
    }
  }

  private def getStringLiteral(chars: List[Char]): (Token, List[Char]) = {

    val startPos = getStartPos

    def endAt(c: Char) = c == '"'

    @tailrec def getStringLiteral(chars: List[Char], s: StringBuilder, parsed: Int): (Token, List[Char]) = chars match {
      case '"' :: r          =>
        val literal = s.toString
        if (literal.length > Lexing.MaximumStringSize) {
          report(StringLiteralTooLarge(literal, parsed + 1))
        }
        (createToken(s.toString, parsed + 1), r)
      case '\r' :: '\n' :: _ =>
        report(UnclosedStringLiteral(parsed))
        (startPos, chars)
      case '\n' :: _         =>
        report(UnclosedStringLiteral(parsed))
        (startPos, chars)
      case '\\' :: r         => r match {
        case 't' :: r  => getStringLiteral(r, s + '\t', parsed + 2)
        case 'b' :: r  => getStringLiteral(r, s + '\b', parsed + 2)
        case 'n' :: r  => getStringLiteral(r, s + '\n', parsed + 2)
        case 'r' :: r  => getStringLiteral(r, s + '\r', parsed + 2)
        case 'f' :: r  => getStringLiteral(r, s + '\f', parsed + 2)
        case '\'' :: r => getStringLiteral(r, s + '\'', parsed + 2)
        case '"' :: r  => getStringLiteral(r, s + '\"', parsed + 2)
        case '\\' :: r => getStringLiteral(r, s + '\\', parsed + 2)
        case '[' :: r  => getStringLiteral(r, s + 27.toChar + '[', parsed + 2)
        case 'u' :: r  => r match {
          case c1 :: c2 :: c3 :: c4 :: r if areHexDigits(c1, c2, c3, c4) =>
            val unicodeNumber = Integer.parseInt("" + c1 + c2 + c3 + c4, 16)
            getStringLiteral(r, s + unicodeNumber.toChar, parsed + 6)
          case _                                                         =>
            endInvalidToken(chars, parsed, endAt, len => report(InvalidUnicode(len)))
        }
        case _         =>
          endInvalidToken(chars, parsed, endAt, len => report(InvalidEscapeSequence(len)))
      }
      case c :: r            => getStringLiteral(r, s + c, parsed + 1)
      case Nil               =>
        report(UnclosedStringLiteral(parsed))
        (startPos, chars)
    }

    getStringLiteral(chars, new StringBuilder, 1)
  }

  private def getMultiLineStringLiteral(chars: List[Char]): (Token, List[Char]) = {
    val startPos = getStartPos

    @tailrec def getStringIdentifier(chars: List[Char], s: StringBuilder): (Token, List[Char]) = chars match {
      case '`' :: r          =>
        column += 1
        val token = STRLIT(s.toString)
        token.setPos(source, startPos.line, startPos.col, line, column)
        (token, r)
      case '\r' :: '\n' :: r =>
        line += 1
        column = 1
        getStringIdentifier(r, s + '\r' + '\n')
      case '\n' :: r         =>
        line += 1
        column = 1
        getStringIdentifier(r, s + '\n')
      case c :: r            =>
        column += 1
        getStringIdentifier(r, s + c)
      case Nil               =>
        report(UnclosedMultilineString(startPos))
        (startPos, chars)
    }

    getStringIdentifier(chars, new StringBuilder)
  }

  private def getHexadecimalLiteral(chars: List[Char]) = {
    @tailrec def getHexadecimalLiteral(chars: List[Char], s: StringBuilder, parsed: Int): (Token, List[Char]) = chars match {
      case '_' :: r                  => getHexadecimalLiteral(r, s, parsed + 1)
      case c :: r if isHexDigit(c)   => getHexadecimalLiteral(r, s + c, parsed + 1)
      case ('l' | 'L') :: r          => (parseLongToken(s.toString, parsed + 1), r)
      case c :: _ if isEndingChar(c) => (parseIntToken(s.toString, parsed), chars)
      case Nil                       => (parseIntToken(s.toString, parsed), chars)
      case _                         => endInvalidToken(chars, parsed, _.isWhitespace, len => report(InvalidHexadecimalLiteral(len)))
    }

    if (chars.isEmpty || isEndingChar(chars.head))
      endInvalidToken(chars, 2, _.isWhitespace, len => report(InvalidHexadecimalLiteral(len)))
    else
      getHexadecimalLiteral(chars, new StringBuilder("0x"), 2)
  }

  private def getBinaryLiteral(chars: List[Char]) = {

    @tailrec def getBinaryLiteral(chars: List[Char], s: StringBuilder, parsed: Int): (Token, List[Char]) = chars match {
      case '_' :: r                  => getBinaryLiteral(r, s, parsed + 1)
      case '0' :: r                  => getBinaryLiteral(r, s + '0', parsed + 1)
      case '1' :: r                  => getBinaryLiteral(r, s + '1', parsed + 1)
      case ('l' | 'L') :: r          => (parseLongToken(s.toString, parsed + 1), r)
      case c :: _ if isEndingChar(c) => (parseIntToken(s.toString, parsed), chars)
      case Nil                       => (parseIntToken(s.toString, parsed), chars)
      case _                         => endInvalidToken(chars, parsed, _.isWhitespace, len => report(InvalidBinaryLiteral(len)))
    }

    if (chars.isEmpty || isEndingChar(chars.head))
      endInvalidToken(chars, 2, _.isWhitespace, len => report(InvalidBinaryLiteral(len)))
    else
      getBinaryLiteral(chars, new StringBuilder("0b"), 2)
  }

  private def getNumberLiteral(chars: List[Char]): (Token, List[Char]) = {
    var foundDecimal = false
    var foundE = false

    @tailrec def getNumberLiteral(chars: List[Char], s: String, parsed: Int): (Token, List[Char]) =
      chars match {
        case '_' :: r            => getNumberLiteral(r, s, parsed + 1)
        case c :: r if c.isDigit => getNumberLiteral(r, s + c, parsed + 1)
        case '.' :: r            =>
          if (!foundDecimal && !foundE) {
            foundDecimal = true
            r match {
              case c :: r if c.isDigit => getNumberLiteral(r, s + "." + c, parsed + 2)
              case _                   => (parseIntToken(s, parsed), chars)
            }
          } else {
            endInvalidToken(chars, parsed, _.isWhitespace, len => report(InvalidNumber(len)))
          }
        case ('e' | 'E') :: r    =>
          if (!foundE) {
            foundE = true
            r match {
              case '-' :: c :: r if c.isDigit => getNumberLiteral(r, s + "E-" + c, parsed + 3)
              case c :: r if c.isDigit        => getNumberLiteral(r, s + "E" + c, parsed + 2)
              case _                          =>
                endInvalidToken(chars, parsed, _.isWhitespace, len => report(InvalidFloat(len)))
            }
          } else {
            endInvalidToken(chars, parsed, _.isWhitespace, len => report(InvalidFloat(len)))
          }
        case ('f' | 'F') :: r    => (parseFloatToken(s, parsed + 1), r)
        case ('l' | 'L') :: r    =>
          if (!foundE && !foundDecimal) {
            (parseLongToken(s, parsed + 1), r)
          } else {
            endInvalidToken(chars, parsed, _.isWhitespace, len => report(InvalidFloat(len)))
          }
        case r                   =>
          if (foundDecimal || foundE) {
            (parseDoubleToken(s, parsed), r)
          } else {
            val rest = if (foundDecimal) '.' :: r else r
            (parseIntToken(s, parsed), rest)
          }
      }

    getNumberLiteral(chars, "", 0)
  }

  private def lineComment(chars: List[Char]): (Token, List[Char]) = {
    @tailrec def lineComment(chars: List[Char], s: StringBuilder): (Token, List[Char]) = chars match {
      case '\r' :: '\n' :: r => (createCommentToken(s.toString, s.length), '\r' :: '\n' :: r)
      case '\n' :: r         => (createCommentToken(s.toString, s.length), '\n' :: r)
      case c :: r            => lineComment(r, s + c)
      case Nil               => (createCommentToken(s.toString, s.length), chars)
    }

    lineComment(chars, new StringBuilder("//"))
  }

  private def blockComment(chars: List[Char]): (Token, List[Char]) = {
    val startPos = getStartPos

    @tailrec def blockComment(chars: List[Char], s: StringBuilder): (Token, List[Char]) = chars match {
      case '*' :: '/' :: r   =>
        column += 2
        val token = COMMENTLIT(s.toString + "*/")
        token.setPos(source, startPos.line, startPos.col, line, column)
        (token, r)
      case '\r' :: '\n' :: r =>
        line += 1
        column = 1
        blockComment(r, s + '\r' + '\n')
      case '\n' :: r         =>
        line += 1
        column = 1
        blockComment(r, s + '\n')
      case c :: r            =>
        column += 1
        blockComment(r, s + c)
      case Nil               =>
        val token = COMMENTLIT(s.toString)
        token.setPos(source, startPos.line, startPos.col, line, column)
        (token, Nil)
    }

    column += 2
    blockComment(chars, new StringBuilder("/*"))
  }

  private def endInvalidToken(
    chars: List[Char],
    parsed: Int,
    endAt: Char => Boolean,
    error: Int => Unit): (Token, List[Char]) = {

    @tailrec def toEnd(chars: List[Char], parsed: Int): (Token, List[Char]) =
      chars match {
        case Nil                =>
          error(parsed)
          (createToken(BAD, parsed), Nil)
        case c :: r if endAt(c) =>
          error(parsed)
          val res = (createToken(BAD, parsed), r)
          if (c == '\r' || c == '\n') {
            line += 1
            column = 1
          }
          res
        case _ :: r             =>
          toEnd(r, parsed + 1)
      }

    toEnd(chars, parsed)
  }

  private def parseIntToken(numStr: String, len: Int): Token = {
    def invalid(len: Int) = {
      report(NumberTooLargeForInt(len))
      createToken(BAD, len)
    }

    val isHex = numStr.startsWith("0x")
    val isBin = numStr.startsWith("0b")
    if (isHex || isBin) {
      val num = numStr.drop(2)
      val radix = if (isHex) 16 else 2
      val maxSize = if (isHex) 8 else 32
      if (num.length > maxSize)
        invalid(len)
      else
        createToken(java.lang.Long.parseLong(num, radix).asInstanceOf[Int], len)
    } else {
      try {
        createToken(numStr.toInt, len)
      } catch {
        case _: NumberFormatException => invalid(len)
      }
    }
  }

  private def getStartPos = createToken(BAD, 0)

  private def parseLongToken(numStr: String, len: Int): Token = {
    def invalid(len: Int) = {
      report(NumberTooLargeForLong(len))
      createToken(BAD, len)
    }

    val isHex = numStr.startsWith("0x")
    val isBin = numStr.startsWith("0b")
    if (isHex || isBin) {
      val num = numStr.drop(2)
      val radix = if (isHex) 16 else 2
      val maxSize = if (isHex) 16 else 64
      if (num.length > maxSize)
        invalid(len)
      else
        createToken(new BigInteger(num, radix).longValue(), len)
    } else {
      try {
        createToken(numStr.toLong, len)
      } catch {
        case _: NumberFormatException => invalid(len)
      }
    }
  }

  // Only accepts valid float and double strings
  private def parseFloatToken(numStr: String, len: Int): Token = createToken(numStr.toFloat, len)

  private def parseDoubleToken(numStr: String, len: Int): Token = createToken(numStr.toDouble, len)

  private def isEndingChar(c: Char) = c.isWhitespace || NonKeywords.contains(s"$c")

  private def areHexDigits(chars: Char*) = chars forall isHexDigit

  private def isHexDigit(c: Char) = c.isDigit || (c.toLower in "abcdef")

  private def createToken(int: Int, tokenLength: Int)(implicit e: Enclosing, l: Line): Token =
    createToken(INTLIT(int), tokenLength)

  private def createToken(long: Long, tokenLength: Int)(implicit e: Enclosing, l: Line): Token =
    createToken(LONGLIT(long), tokenLength)

  private def createToken(float: Float, tokenLength: Int)(implicit e: Enclosing, l: Line): Token =
    createToken(FLOATLIT(float), tokenLength)

  private def createToken(double: Double, tokenLength: Int)(implicit e: Enclosing, l: Line): Token =
    createToken(DOUBLELIT(double), tokenLength)

  private def createToken(kind: TokenKind, tokenLength: Int)(implicit e: Enclosing, l: Line): Token =
    createToken(new Token(kind), tokenLength)

  private def createIdToken(string: String, tokenLength: Int)(implicit e: Enclosing, l: Line): Token =
    createToken(ID(string), tokenLength)

  private def createCommentToken(str: String, tokenLength: Int)(implicit e: Enclosing, l: Line): Token =
    createToken(COMMENTLIT(str), tokenLength)

  private def createToken(char: Char, tokenLength: Int)(implicit e: Enclosing, l: Line): Token =
    createToken(CHARLIT(char), tokenLength)

  private def createToken(string: String, tokenLength: Int)(implicit e: Enclosing, l: Line): Token =
    createToken(STRLIT(string), tokenLength)

  private def createToken(token: Token, tokenLength: Int)(implicit e: Enclosing, l: Line): Token = {
    val endCol = column + tokenLength
    token.setPos(source, line, column, line, endCol)
    debug"Creating token $token with position ($line, $column, $line, $endCol)"
    column += tokenLength
    token
  }
}
