package tlang.compiler

import java.io.{File, FileNotFoundException}

import tlang.compiler.analyzer.{FlowAnalysis, NameAnalysis, TypeChecking}
import tlang.compiler.ast.Parser
import tlang.compiler.ast.Trees.CompilationUnit
import tlang.compiler.code.{CodeGeneration, Desugaring}
import tlang.compiler.error.CompilationException
import tlang.compiler.lexer.Lexer
import tlang.compiler.modification.Templates
import tlang.utils.Extensions._
import tlang.utils.{FileSource, ProgramExecutor, Source}

/**
  * Created by Tim Lindeberg on 4/11/2016.
  */
trait ValidTester extends Tester {

  import Tester._

  val programExecutor = ProgramExecutor()

  override def Pipeline: Pipeline[Source, CompilationUnit] =
    Lexer andThen Parser andThen Templates andThen NameAnalysis andThen TypeChecking andThen FlowAnalysis

  def testFile(file: File): Unit = {
    val ctx = getTestContext(Some(file))

    try {
      val sources = FileSource(file) :: Nil
      val cus = Pipeline.run(ctx)(sources)

      ctx.reporter.hasErrors should be(false)

      val compilation = Desugaring andThen CodeGeneration
      compilation.run(ctx)(cus)
      val res = programExecutor(ctx, file).getOrElse(fail(s"Test timed out!"))
      val resLines = lines(res)
      val sol = parseSolutions(file)
      assertCorrect(resLines, sol)
    } catch {
      case t: CompilationException  =>
        println(t.getMessage)
        fail("Compilation failed")
      case _: FileNotFoundException => fail(s"Invalid test, file not found: ${file.getPath}")
    }
  }

  private def lines(str: String): List[String] = str.split("\\r?\\n").map(_.trim).toList

  private def parseSolutions(file: File): List[(Int, String)] = {
    val fileName = file.getPath
    using(io.Source.fromFile(fileName)) { source =>
      source.getLines().zipWithIndex.collect {
        case (SolutionRegex(line), lineNumber) => (lineNumber + 1, line.trim)
      }.toList
    }
  }

  private def assertCorrect(results: List[String], solutions: List[(Int, String)]): Unit = {
    def extraInfo(i: Int) = formatTestFailedMessage(i + 1, results, solutions.map(_._2))
    results.zip(solutions).zipWithIndex.foreach {
      case ((res, (line, sol)), i) =>
        if (res != sol)
          fail(s"Expected '$sol' but found '$res' at line $line ${extraInfo(i)}")
    }
    if (results.length != solutions.length) {
      fail(s"Expected ${solutions.length} lines but ${results.length} were output ${extraInfo(-1)}")
    }
  }


}
