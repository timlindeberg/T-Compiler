package tlang.utils.formatting

import tlang.utils.Extensions._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class WordWrapper(tabSize: Int = 2, wrapAnsiColors: Boolean = true) {


  val BreakCharacters = "-/.,_\\;:()[]{}"
  val WhiteSpaces     = " \t"

  def apply(text: String, maxWidth: Int): List[String] = {
    val lineBuffer: ListBuffer[String] = ListBuffer()

    text.replaceAll("\r\n", "\n").split("\n", -1) foreach { wrap(_, maxWidth, lineBuffer) }

    val lines = lineBuffer.toList
    if (wrapAnsiColors) wrapAnsiFormatting(lineBuffer.toList) else lines
  }

  private def wrap(line: String, maxWidth: Int, lines: ListBuffer[String]): Unit = {
    if (line.isEmpty) {
      lines += ""
      return
    }

    var hasWrapped = false

    var (spaceLen, lineLen, wordLen) = (0, 0, 0)
    var wordStart = 0
    val currentLine = new StringBuilder(2 * maxWidth) // to make room for one line and hopefully some ANSI escape codes

    def addLine(): Unit = {
      if (lineLen == 0)
        return

      lines += currentLine.toString
      currentLine.clear()

      hasWrapped = true
      lineLen = 0
    }

    def addWordToLine(word: String, len: Int) = {
      currentLine ++= word
      lineLen += len
      wordLen = 0
    }

    def addSpacesToLine(len: Int): Int = {
      // If we're at the start of a line and already have wrapped we don't
      // want to add any spaces. We should only preserve indentation for the first
      // line.
      if (lineLen == 0 && hasWrapped)
        return 0

      // If we can make it fit by removing some spaces
      // we do so, otherwise use all the spaces
      val numSpaces = Math.min(spaceLen, maxWidth - (lineLen + len))
      currentLine.appendTimes(' ', numSpaces)
      numSpaces
    }

    // When reaching the end of a word (or the end of input) we need to determine
    // whether we add the word to the current line, put it on the next line or
    // split the word up
    def endOfWord(endIndex: Int): Unit = {
      if (wordLen == 0)
        return

      // The word including potential ansi formatting
      var word = line.substring(wordStart, endIndex)
      // The word fits on the line if we use one space
      if (lineLen + 1 + wordLen <= maxWidth) {
        val numSpaces = addSpacesToLine(wordLen)
        addWordToLine(word, wordLen + numSpaces)
        return
      }

      // The word doesn't fit on the current line but is smaller than one line
      // so we add it to the next line
      if (lineLen + 1 + wordLen > maxWidth && wordLen <= maxWidth) {
        addLine()
        // Skip spaces when wrapping the line since we never want to start
        // a wrapped line with spaces
        addWordToLine(word, wordLen)
        return
      }

      // If we got here the word is larger than one line,
      // we have to split it up
      while (wordLen > maxWidth) {
        // If there is content on the line we need an additional space
        val spaceLeft = if (lineLen == 0) maxWidth else maxWidth - lineLen - 1
        val (breakPoint, len) = findBreakpoint(word, spaceLeft)
        val restLen = wordLen - len
        val (w, nextPart) = word.splitAt(breakPoint)
        if (breakPoint == -1) {
          // If we couldn't find a place to split the word we use the line as it is
          addLine()
        } else if (len <= spaceLeft) {
          // The split word fits on the given line, add the split and then the line
          val numSpaces = addSpacesToLine(len)
          addWordToLine(w, len + numSpaces)
          addLine()
        } else {
          // The split word does not fit on the line, add it first and add the split
          // to the next line
          addLine()
          addWordToLine(w, len)
        }
        wordLen = restLen
        word = nextPart
      }
      if (wordLen > 0)
        addWordToLine(word, wordLen)
      wordLen = 0
    }

    var i = 0
    while (i < line.length) {
      val c = line(i)
      c match {
        case '\u001b' if line(i + 1) == '[' =>
          // Skip past ANSI characters since they are not added towards the word length
          i = line.indexOf('m', i + 1)
        case c if c in WhiteSpaces          =>
          endOfWord(i)
          spaceLen = if (c == '\t') tabSize else 1
          while (i + 1 < line.length && (line(i + 1) in WhiteSpaces)) {
            spaceLen += (if (line(i + 1) == '\t') tabSize else 1)
            i += 1
          }
          wordStart = i + 1
        case _                              =>
          wordLen += 1
      }
      i += 1
    }

    endOfWord(i)
    if (lineLen > 0)
      addLine()
  }

  // Locates a place to break the word up. If there exists any special characters in the string,
  // or if there exists a place where the string goes from lowercase to uppercase it will break
  // at the last such character otherwise it will break at the last possible character for the
  // given width.
  // Returns the break point index and the number of visible characters up to that point
  private def findBreakpoint(word: String, widthAvailable: Int): (Int, Int) = {
    if (widthAvailable <= 0)
      return (-1, 0)

    var i = 0
    var breakPoint = -1
    var breakLen = 0
    var len = 0
    while (i < word.length) {
      word(i) match {
        case '\u001b' if word(i + 1) == '[' =>
          // Don't count the length of ANSI characters
          i = word.indexOf('m', i + 1)
        case c                              =>
          len += 1

          val isCamelCase = i + 1 < word.length && c.isLower && word(i + 1).isUpper
          if ((c in BreakCharacters) || isCamelCase) {
            breakPoint = i + 1
            breakLen = len
          }
          if (len == widthAvailable)
            return if (breakPoint == -1) (i + 1, len) else (breakPoint, breakLen)
      }
      i += 1
    }
    (breakPoint, 0)
  }


  private def wrapAnsiFormatting(lines: List[String]): List[String] = {
    var SGRs = mutable.Set[Int]()
    var color = -1
    var BGColor = -1
    var ansi = ""

    def clearAnsi(): Unit = {
      color = -1
      BGColor = -1
      SGRs.clear()
    }

    // Updates the current state of ANSI colors by looking at the ANSI escape
    // code at line(index)
    def updateFrom(line: String, startIndex: Int): Int = {
      var i = startIndex
      while (i < line.length && line(i) == '\u001b' && line(i + 1) == '[') {
        val endOfAnsi = line.indexOf('m', i + 1)

        val ansiValues = line.substring(i + 2, endOfAnsi).split(";").map(_.toInt)
        ansiValues.foreach {
          case 0                       => clearAnsi()
          case v if v in (1 until 10)  => SGRs += v
          case v if v in (30 until 40) => color = v
          case v if v in (40 until 50) => BGColor = v
          case _                       =>
        }
        // Guarantees the same order each time
        val escapes = (color :: BGColor :: SGRs.toList)
          .filter(_ != -1)
          .sorted

        ansi = if (escapes.isEmpty) "" else escapes.mkString("\u001b[", ";", "m")
        i = endOfAnsi + 1
      }
      i
    }

    def updateAnsi(line: String): Unit = {
      var i = 0
      while (i < line.length) {
        line(i) match {
          case '\u001b' if line(i + 1) == '[' => i = updateFrom(line, i)
          case _                              =>
        }
        i += 1
      }
    }

    lines.map { line =>
      var sb = new StringBuilder
      if (line.startsWith("\u001b[")) {
        // Make sure we don't repeat the previous ansi if it is immediately updated
        val endOfAnsi = updateFrom(line, 0)
        sb ++= ansi
        sb ++= line.substring(endOfAnsi)
      } else {
        sb ++= ansi
        sb ++= line
      }
      updateAnsi(line)
      if (ansi.nonEmpty && !line.endsWith(Console.RESET))
        sb ++= Console.RESET
      sb.toString
    }
  }

}
