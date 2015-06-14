package koolc

import java.io.File

import koolc.analyzer.Types._
import koolc.ast.Trees
import koolc.ast.Trees._
import koolc.lexer.Token

import scala.io.Source
import scala.sys.process.ProcessLogger

object TestUtils {
  val runScript = "./reference/run.sh"
  val resources = "./src/test/resources/"
  val solutionPrefix = ".kool-solution"

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
      if (options.size > 0) {
        expectedErrors = options(0).toInt
        quietReporter = options.lift(1).getOrElse("false").toBoolean
      }
    } catch {
      case _: Throwable => expectedErrors = 1
    }
    map("expectedErrors" -> expectedErrors, "quietReporter" -> quietReporter)
  }
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
        case node: Typed => hasTypes = node.getType != TUntyped
      })
      hasTypes
    }

    private def correctTypes(t: Tree): Boolean = {
      var types = true
      Trees.traverse(t, (_, x) => {
        types = x match {
          case x@IntLit(value)            => x.getType == TInt
          case x@StringLit(value)         => x.getType == TString
          case x@Identifier(value)        => x.getType != TUntyped && x.getType != TError
          case x@TypeIdentifier(value, _) => x.getType != TUntyped && x.getType != TError
          case x@IntType()                => x.getType == TInt
          case x@IntArrayType()           => x.getType == TIntArray
          case x@BooleanType()            => x.getType == TBool
          case x@StringType()             => x.getType == TString
          case x@True()                   => x.getType == TBool
          case x@False()                  => x.getType == TBool
          case x@This()                   => x.getType != TUntyped && x.getType != TError
          case _                          => true
        }
      })
      types
    }
  }
}

