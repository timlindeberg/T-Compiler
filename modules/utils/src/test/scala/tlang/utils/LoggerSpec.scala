package tlang
package utils

import java.util.Date

import tlang.formatting.Formatter
import tlang.testutils.UnitSpec
import tlang.testutils.snapshot.SnapshotTesting

class LoggerSpec extends UnitSpec with SnapshotTesting {

  behavior of "A logger"

  it should "only output at the correct log levels" in {
    def doLog(logLevel: LogLevel): String = {
      val (logger, fileBuffer) = getLogger(logLevel)

      logger.trace("A")
      logger.debug("B")
      logger.info("C")
      logger.warn("D")
      logger.error("E")
      fileBuffer.toString
    }

    test("Trace") {
      val content = doLog(LogLevel.Trace)
      content should include("A" + NL)
      content should include("B" + NL)
      content should include("C" + NL)
      content should include("D" + NL)
      content should include("E" + NL)
    }

    test("Debug") {
      val content = doLog(LogLevel.Debug)

      content should not include "A" + NL
      content should include("B" + NL)
      content should include("C" + NL)
      content should include("D" + NL)
      content should include("E" + NL)
    }

    test("Info") {
      val content = doLog(LogLevel.Info)

      content should not include "A" + NL
      content should not include "B" + NL
      content should include("C" + NL)
      content should include("D" + NL)
      content should include("E" + NL)
    }

    test("Warn") {
      val content = doLog(LogLevel.Warn)

      content should not include "A" + NL
      content should not include "B" + NL
      content should not include "C" + NL
      content should include("D" + NL)
      content should include("E" + NL)
    }

    test("Error") {
      val content = doLog(LogLevel.Error)

      content should not include "A" + NL
      content should not include "B" + NL
      content should not include "C" + NL
      content should not include "D" + NL
      content should include("E" + NL)
    }

    test("Off") {
      val content = doLog(LogLevel.Off)

      content should not include "A" + NL
      content should not include "B" + NL
      content should not include "C" + NL
      content should not include "D" + NL
      content should not include "E" + NL
    }
  }

  it should "minimize the location of the log call" in {
    val (sb, memFile) = memoryFile()
    val logSettings = LoggingSettings(
      printToStdout = false,
      formatter = Formatter.SimpleFormatter,
      logLevel = LogLevel.Info,
      logThreads = true
    )
    logSettings.printToFile = List(memFile)

    var testEnclosing: String = ""
    var testFile: String = ""
    var testLineNumber: Int = 0

    val logger: Logger = new Logger()(logSettings) {
      override val now: Date = new Date(0)
      override val threadName: String = "TestThread"
      override val threadId: Int = 0
      override def location(enclosing: String, file: String, lineNumber: Int): String =
        super.location(testEnclosing, testFile, testLineNumber)
      override def fileName(file: String): String = file
    }

    test("name that already fits") {
      testEnclosing = "a.b.c.AbcDef"
      testFile = "Abc.scala"
      testLineNumber = 25
      logger.info("ABC")

      sb.toString shouldBe s"INFO  | [TestThread] 01:00:00:000 a.b.c.AbcDef(Abc.scala:25)                    ABC$NL"
      sb.clear()
    }

    test("partially truncated name") {
      testEnclosing = "abcdefgh.abcdefgh.abcdefgh.Abcdefgh"
      testFile = "Abcdefghijklmno.scala"
      testLineNumber = 25
      logger.info("ABC")

      sb.toString shouldBe s"INFO  | [TestThread] 01:00:00:000 a.a.a.Abcdefgh(Abcdefghijklmno.scala:25)      ABC$NL"
      sb.clear()
    }

    test("fully truncated name") {
      testEnclosing = "abcdefgh.abcdefgh.abcdefgh.AbcdefghAbcdefgh"
      testFile = "Abcdefghijklmno.scala"
      testLineNumber = 25
      logger.info("ABC")

      sb.toString shouldBe s"INFO  | [TestThread] 01:00:00:000 a.a.a.A(Abcdefghijklmno.scala:25)             ABC$NL"
      sb.clear()
    }

    test("name that fits when removing prefixes") {
      testEnclosing = "a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.x.y.z.Abc"
      testFile = "Abcdefghijklmno.scala"
      testLineNumber = 25
      logger.info("ABC")

      sb.toString shouldBe s"INFO  | [TestThread] 01:00:00:000 q.r.s.t.u.v.x.y.z.A(Abcdefghijklmno.scala:25) ABC$NL"
      sb.clear()
    }

    test("name that doesn't fit at all") {
      testEnclosing = "abcdefgh.abcdefgh.abcdefgh.AbcdefghAbcdefgh"
      testFile = "Abcdefghijklmnopqrstuvxyz1234567890.scala"
      testLineNumber = 25
      logger.info("ABC")

      sb.toString shouldBe s"INFO  | [TestThread] 01:00:00:000 ...defghijklmnopqrstuvxyz1234567890.scala:25) ABC$NL"
      sb.clear()
    }
  }

  it should "apply colors and correct formatting" in {
    val (logger, fileBuffer) = getLogger(useColor = true)
    logger.trace("ABC")
    logger.debug("DEF")
    logger.info("GHI")
    logger.warn("JKL")
    logger.error("MNO")

    fileBuffer.toString should matchSnapshot
  }

  it should "output extra info" in {
    val (logger, fileBuffer) = getLogger(useColor = true)
    logger.info("ABC", s"ABC${ NL }DEF${ NL }GHI")
    logger.info("DEF")
    logger.error("GHI", s"ABC${ NL }DEF${ NL }GHI")

    fileBuffer.toString should matchSnapshot
  }

  it should "log with string context" in {
    test("with colors") {
      val (logger, fileBuffer) = getLogger(useColor = true)

      // info"$1ABC$2DEF$3"
      logger.logWithContext(LogLevel.Info, new StringContext("", "ABC", "DEF", ""), Seq(1, 2, 3))
      // info"<$1, $2, $3>"
      logger.logWithContext(LogLevel.Info, new StringContext("<", ", ", ", ", ">"), Seq(1, 2, 3))
      // info"ABC: $1"
      logger.logWithContext(LogLevel.Info, new StringContext("ABC: ", ""), Seq(s"A${ NL }multiline${ NL }string!"))

      fileBuffer.toString should matchSnapshot
    }

    test("without colors") {
      val (logger, fileBuffer) = getLogger(useColor = false)

      // info"$1ABC$2DEF$3"
      logger.logWithContext(LogLevel.Info, new StringContext("", "ABC", "DEF", ""), Seq(1, 2, 3))
      // info"<$1, $2, $3>"
      logger.logWithContext(LogLevel.Info, new StringContext("<", ", ", ", ", ">"), Seq(1, 2, 3))
      // info"ABC: $1"
      logger.logWithContext(LogLevel.Info, new StringContext("ABC: ", ""), Seq(s"A${ NL }multiline${ NL }string!"))

      fileBuffer.toString should matchSnapshot
    }
  }

  private def getLogger(logLevel: LogLevel = LogLevel.Trace, useColor: Boolean = false): (Logger, StringBuilder) = {
    val (sb, memFile) = memoryFile()

    val formatter = Formatter(useColor = useColor)

    val logSettings = LoggingSettings(
      logLevel = logLevel,
      formatter = formatter,
      printToStdout = false,
      logThreads = true
    )
    logSettings.printToFile = List(memFile)

    var _threadId = 0
    val logger: Logger = new Logger()(logSettings) {
      override val now: Date = new Date(0)
      override def threadName: String = "TestThread" + _threadId
      override def threadId: Int = { val t = _threadId; _threadId += 1; t }
      override def location(enclosing: String, file: String, lineNumber: Int): String = super.location(enclosing, file, 0)
    }

    (logger, sb)
  }
}
