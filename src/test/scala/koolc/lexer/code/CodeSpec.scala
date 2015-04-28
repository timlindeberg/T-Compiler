package koolc.lexer.code

import org.scalatest._
import scala.sys.process._
import koolc.utils.Context
import java.io.File
import koolc.lexer.Token
import koolc.lexer.Lexer
import koolc.ast._
import koolc.TestUtils
import koolc.utils.CompilationException
import scala.io.Source
import koolc.ast.Trees.Program
import scala.collection.mutable.HashMap._
import koolc.analyzer.Types._
import koolc.analyzer.Symbols.ClassSymbol
import koolc.analyzer.NameAnalysis;
import koolc.code.CodeGeneration
import koolc.analyzer.TypeChecking

class CodeSpec extends FlatSpec with Matchers {
  val flag = "--eval"

  behavior of "Given tests"
  TestUtils.programFiles(TestUtils.resources + "/given/ast/valid/").foreach { file =>
    it should "code gen program " + file.toPath() in test(file)
  }

  def test(file: File, exception: Boolean = false) = {
    val program = Source.fromFile(file).mkString
    val ctx = new Context(reporter = new koolc.utils.Reporter, file = file, outDir = None)
    def analysis(p: Program) = NameAnalysis.run(ctx)(p)
    def tcheck(p: Program) = TypeChecking.run(ctx)(p)
    def code(p: Program) = CodeGeneration.run(ctx)(p)
    def parse(p: String) = Parser.run(ctx)(Lexer.run(p.toList, ctx.file))
    def print(p: Program) = Printer(p, true)
    code(tcheck(analysis(parse(program))))
    //assert(res + "\n" == correct)
  }

  def getAnswer(file: File) = Seq(TestUtils.runScript, flag + " " + file.toPath()) !!

}