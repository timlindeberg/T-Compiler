package tlang.repl

import akka.actor.ActorRef
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, Matchers}
import tlang.formatting.DefaultFormatting
import tlang.options.Options
import tlang.repl.actors.ReplActor.{Start, Stop}
import tlang.testutils.{AnsiMatchers, SnapshotTesting}

class ReplSnapshotSpec extends AsyncFlatSpec with SnapshotTesting with Matchers with AnsiMatchers with BeforeAndAfterAll {

  def Width = 80
  def Height = 300
  def TimeoutMilliseconds = 5000

  val testTerminal: TestTerminal = new TestTerminal(Width, Height, TimeoutMilliseconds)
  var repl        : ActorRef     = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    val formatting = DefaultFormatting.copy(lineWidth = Width)
    repl = Main.createRepl(testTerminal, Options.Empty, formatting)
    repl ! Start
  }

  override def afterAll(): Unit = {
    super.afterAll()
    repl ! Stop
  }

  // These tests are executed in order and depend on the previous tests.

  it should "execute simple commands" in {
    testTerminal
      .executeCommand("4 + 4")
      .stopWhen { box => box.contains("esult") }
      .display
      .map { _ should matchSnapshot }
  }


  it should "use existing variables" in {
    testTerminal
      .executeCommand("5 * res0")
      .stopWhen { box => box.contains("Result") }
      .display
      .map { _ should matchSnapshot }
  }


  it should "define and use classes" in {
    testTerminal
      .executeCommand(
        s"""|class A =
            |\tDef Times(t: Int) =
            |\tfor(var i = 0; i < t; i++)
            |\tprintln("A" + i)
            |""".stripMargin,
        KeyType.Backspace,
        KeyType.Backspace,
        KeyType.Backspace,
        "new A().Times(10)"
      )
      .stopWhen { box => box.contains("Result") }
      .display
      .map { _ should matchSnapshot }
  }


  it should "define and use functions" in {
    testTerminal
      .executeCommand(
        s"""|def Fun(a: Int, b: String) = b * a
            |
            |Fun(5, "ABC")
            |""".stripMargin
      )
      .stopWhen { box => box.contains("Result") }
      .display
      .map { _ should matchSnapshot }
  }


  it should "handle imports" in {
    testTerminal
      .executeCommand(
        s"""|import java::util::Date
            |
            |new Date(0)
            |""".stripMargin
      )
      .stopWhen { box => box.contains("Result") }
      .display
      .map { _ should matchSnapshot }
  }


  it should "show compilation errors" in {
    testTerminal
      .executeCommand(""""ABC" + a + res0 + b + "ABC"""")
      .stopWhen { box => box.contains("Error") }
      .display
      .map { _ should matchSnapshot }
  }


  it should "show stack traces on errors" in {
    testTerminal
      .executeCommand("""error("ABC")""")
      .stopWhen { box => box.contains("Error") }
      .display
      .map { _ should matchSnapshot }
  }


  it should "exit when pressing CTRL+C" in {
    testTerminal
      .executeCommand(new KeyStroke('c', true, false))
      .stopWhen { box => box.contains("Thanks") }
      .display
      .map { _ should matchSnapshot }
  }

}
