package tlang.compiler.ast

import java.io.File

import org.scalatest.{FunSuite, Matchers}
import tlang.compiler.lexer.Lexer
import tlang.compiler.{Context, Tester}
import tlang.utils.{Colors, FileSource, StringSource}

/**
  * Created by Tim Lindeberg on 3/7/2017.
  */
class PrettyPrinterSpec extends FunSuite with Matchers {

  private val TestFile   : String  = Tester.Resources + "positions/ParserPositions.t"
  private val TestContext: Context = Tester.testContext


  private val parser        = (Lexer andThen Parser).run(TestContext) _
  private val prettyPrinter = PrettyPrinter(Colors(isActive = false))

  test("Tree is the same after being pretty printed and reparsed") {
    val file = FileSource(new File(TestFile)) :: Nil

    val CU = parser(file).head

    val printedCU = prettyPrinter(CU)
    val reparsedCU = parser(StringSource(printedCU, "ParserPositions") :: Nil).head

    CU shouldBe reparsedCU
    printedCU shouldBe prettyPrinter(reparsedCU)
  }

}