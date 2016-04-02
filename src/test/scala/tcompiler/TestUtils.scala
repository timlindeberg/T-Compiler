package tcompiler

import java.io.File

import tcompiler.analyzer.Types._
import tcompiler.ast.Trees
import tcompiler.ast.Trees._
import tcompiler.lexer.Token
import tcompiler.utils.Positioned

import scala.io.Source
import scala.sys.process.{ProcessLogger, _}

object TestUtils {

  val runScript = "./reference/run.sh"
  val resources = "./src/test/resources/"
  val solutionPrefix = ".kool-solution"

  val Interpreter = new Interpreter

  def executeTProgram(f: File, prefix: String): String = "java -cp " + prefix + f.getName + " " + f.getName.dropRight(Main.FileEnding.length) !!

  def lines(str: String) = str.split("\\r?\\n").toList

  def programFiles(dir: String): Array[File] = {
    val f = new File(dir)
    if (f.exists()) {
      f.listFiles.filter(x => !x.getName.contains(solutionPrefix) && x.getName != ".DS_Store")
    } else {
      f.mkdir
      Array[File]()
    }
  }
  def format(token: Token): String = token + "(" + token.line + ":" + token.col + ")"

  def readOptions(file: File): Map[String, Any] = {
    val progString = Source.fromFile(file).getLines.toList
    var expectedErrors = 1
    var quietReporter = false
    val map = Map
    try {
      val options = progString.head.split(" ").tail.toList
      if (options.nonEmpty) {
        expectedErrors = options(0).toInt
        quietReporter = options.lift(1).getOrElse("false").toBoolean
      }
    } catch {
      case _: Throwable => expectedErrors = 1
    }
    map("expectedErrors" -> expectedErrors, "quietReporter" -> quietReporter)
  }
  val ErrorRegex = """\[.*\] (Fatal|Warning|Error) \((.*)\).*""".r
  val SolutionRegex = """.*// *res:(.*)""".r
  val SolutionOrderedRegex = """.*// *res(\d+):(.*)""".r

  def parseSolutions(file: File): List[String] = {
    val fileName = file.getPath
    var i = -1
    val answers = Source.fromFile(fileName).getLines().map(_ match {
      case SolutionOrderedRegex(num, result) => (num.toInt, result)
      case SolutionRegex(result)             =>
        i += 1
        (i, result)
      case _ => (-1, "")
    })
    answers.toList.filter(_._1 >= 0).sortWith(_._1 < _._1).map(_._2)
  }

  // Parses codes from error messages
  def parseErrorCodes(errorMessages: String) =
    errorMessages.split("\n\n\n").map(_.split("\n")(0)).collect {
        case ErrorRegex(_, errorCode) => errorCode
    }.toList

  object IgnoreErrorOutput extends ProcessLogger {
    def buffer[T](f: ⇒ T): T = f
    def err(s: ⇒ String): Unit = {}
    def out(s: ⇒ String): Unit = {}
  }

  object HasTypes {

    def apply(prog: Program): Boolean = hasTypes(prog) && correctTypes(prog)

    def withoutMethodCalls(prog: Program): Boolean = hasTypes(prog)

    private def hasTypes(prog: Program) = {
      var hasTypes = true
      Trees.traverse(prog, (_, curr) => Some(curr) collect {
        case _: Empty    =>
        case node: Typed =>
          assert(node.getType != TUntyped, node + " was untyped. (line: " + node.asInstanceOf[Positioned].line + ")")
          if (node.getType == TUntyped) hasTypes = false
      })
      hasTypes
    }

    private def correctTypes(t: Tree): Boolean = {
      var types = true
      Trees.traverse(t, (_, x) => {
        types = x match {
          case x: IntLit          => x.getType == TInt
          case x: StringLit       => x.getType == TString
          case x: Identifier      => x.getType != TUntyped && x.getType != TError
          case x: ClassIdentifier => x.getType != TUntyped && x.getType != TError
          case x: IntType         => x.getType == TInt
          case x: ArrayType       => x.getType == TArray
          case x: BooleanType     => x.getType == TBool
          case x: StringType      => x.getType == TString
          case x: True            => x.getType == TBool
          case x: False           => x.getType == TBool
          case x: This            => x.getType != TUntyped && x.getType != TError
          case _                  => true
        }
      })
      types
    }
  }
}

