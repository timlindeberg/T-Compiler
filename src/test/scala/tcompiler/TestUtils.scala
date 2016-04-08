package tcompiler

import java.io.File

import org.scalatest.FlatSpec
import tcompiler.analyzer.Types._
import tcompiler.ast.Trees
import tcompiler.ast.Trees._
import tcompiler.lexer.Token

import scala.io.Source
import scala.sys.process.{ProcessLogger, _}

object TestUtils extends FlatSpec {

  val Resources      = "./src/test/resources/"
  val SolutionPrefix = ".kool-solution"
  val Interpreter    = new Interpreter

  def test(file: File, testFunction: File => Unit): Unit = {
    if (file.isDirectory){
      programFiles(file.getPath).foreach(test(_, testFunction))
    } else{
      if(shouldBeIgnored(file))
        ignore should file.getName.toString in testFunction(file)
      else
        it should file.getName.toString in testFunction(file)
    }
  }

  def executeTProgram(f: File, prefix: String): String =
    executeTProgram(prefix + f.getName, f.getName.dropRight(Main.FileEnding.length))

  def executeTProgram(classPath: String, mainName: String): String =
    s"java -cp $classPath $mainName" !!

  def lines(str: String) = str.split("\\r?\\n").toList

  def programFiles(dir: String): Array[File] = {
    val f = new File(dir)
    if (!f.isDirectory)
      return Array[File](f)

    if (f.exists()) {
      f.listFiles.filter(x => !x.getName.contains(SolutionPrefix) && x.getName != ".DS_Store")
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


  def parseSolutions(file: File): List[String] = {
    val SolutionRegex = """.*// *[R|r]es:(.*)""".r
    val SolutionOrderedRegex = """.*// *[R|r]es(\d+):(.*)""".r

    val fileName = file.getPath
    var i = -1
    val answers = Source.fromFile(fileName).getLines().map(_ match {
      case SolutionOrderedRegex(num, result) => (num.toInt, result)
      case SolutionRegex(result)             =>
        i += 1
        (i, result)
      case _                                 => (-1, "")
    })
    answers.toList.filter(_._1 >= 0).sortWith(_._1 < _._1).map(_._2)
  }

  def shouldBeIgnored(file: File): Boolean  = {
    val firstLine = Source.fromFile(file.getPath).getLines().take(1).toList.head
    firstLine.matches(""".*// *[I|i]gnore.*""")
  }

  // Parses codes from error messages
  def parseErrorCodes(errorMessages: String) = {
    val ErrorRegex = """(Fatal|Warning|Error) \((.+?)\).*""".r

    removeANSIFormatting(errorMessages).split("\n\n\n").map(_.split("\n")(1)).collect {
      case ErrorRegex(_, errorCode) => errorCode
    }.toList
  }

  def assertCorrect(res: List[String], sol: List[String]) = {
    assert(res.length == sol.length, "Different amount of results and expected results.")

    flattenTuple(res.zip(sol).zipWithIndex).foreach {
      case (r, s, i) =>
        assert(r.trim == s.trim, s": error on test ${i + 1} \n ${resultsVersusSolution(res, sol)}")
    }
  }

  private def flattenTuple[A, B, C](t: List[((A, B), C)]): List[(A, B, C)] = t.map(x => (x._1._1, x._1._2, x._2))

  private def resultsVersusSolution(res: List[String], sol: List[String]) = {
    val numbers = (1 to res.size).map(_.toString)
    val numbered = flattenTuple(numbers.zip(res).zip(sol).toList)
    val list = ("", "Result:", "Solution:") :: numbered
    list.map { case (i, r, s) => f"$i%-4s$r%-20s$s%-20s" }.mkString("\n")
  }

    def hasTypes(prog: Program) = {
    var hasTypes = true
    Trees.traverse(prog, (_, curr) => Some(curr) collect {
      case _: Empty    =>
      case node: Typed =>
        if (node.getType == TUntyped) hasTypes = false
    })
    hasTypes && correctTypes(prog)
  }

  private def removeANSIFormatting(s: String) = """\x1b[^m]*m""".r.replaceAllIn(s, "")


  private def correctTypes(t: Tree): Boolean = {
    var types = true
    Trees.traverse(t, (_, x) => {
      types = x match {
        case x: IntLit          => x.getType == TInt
        case x: StringLit       => x.getType == TString
        case x: Identifier      => x.getType != TUntyped && x.getType != TError
        case x: ClassIdentifier => x.getType != TUntyped && x.getType != TError
        case x: IntType         => x.getType == TInt
        case x: ArrayType       => x.getType == tArray
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

  object IgnoreErrorOutput extends ProcessLogger {
    def buffer[T](f: ⇒ T): T = f
    def err(s: ⇒ String): Unit = {}
    def out(s: ⇒ String): Unit = {}
  }
}

