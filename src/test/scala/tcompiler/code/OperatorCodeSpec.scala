package tcompiler.code

import java.io.{File, FileWriter, IOException}

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import tcompiler.analyzer.Symbols.{ClassSymbol, MethodSymbol, VariableSymbol}
import tcompiler.analyzer.Types._
import tcompiler.analyzer.{NameAnalysis, Symbols, TypeChecker, TypeChecking}
import tcompiler.ast.Trees._
import tcompiler.ast.{Parser, Printer}
import tcompiler.lexer.Lexer
import tcompiler.modification.{Imports, Templates}
import tcompiler.utils.{Context, Reporter}
import tcompiler.{Main, TestUtils}

import scala.util.Random

/**
 * Created by Tim Lindeberg on 4/2/2016.
 */
class OperatorCodeSpec extends FlatSpec with Matchers with BeforeAndAfter {

  val Flag = "--eval"

  val TestFolder   = "./tmpTest"
  val TestFilePath = TestFolder + "/tmpTestFile.kool"

  var testFolderFile = new File(TestFolder)
  var testFile       = new File(TestFilePath)

  val Compiler     = Lexer andThen Parser andThen Templates andThen Imports andThen NameAnalysis andThen TypeChecking andThen CodeGeneration
  val Rand         = new Random()
  val TestCtx      = new Context(reporter = new Reporter(suppressWarnings = true), file = testFile, outDir = Some(testFolderFile))
  val TypeCheckCtx = new Context(reporter = new Reporter(suppressWarnings = true), file = testFile, outDir = None)
  val ClassSymbol  = new ClassSymbol("obj")
  val MethodDecl   = new MethodDecl(None, Identifier(""), List(), Block(List()), Set(Private()))
  val MainMethod   = new MethodSymbol("main", ClassSymbol, MethodDecl).setType(TUnit)
  val TypeChecker  = new TypeChecker(TypeCheckCtx, MainMethod)



  val MainName      = "Main"
  val IdName        = "x"
  val StringLength  = 5
  val NumberOfTests = 0

  val int    = () => IntLit(Rand.nextInt).setType(TInt)
  val bool   = () => (if (Rand.nextBoolean) True() else False()).setType(TBool)
  val long   = () => LongLit(Rand.nextLong % Int.MaxValue).setType(TLong)
  val float  = () => FloatLit(Rand.nextFloat).setType(TFloat)
  val double = () => DoubleLit(Rand.nextDouble % Float.MaxValue).setType(TDouble)
  val char   = () => CharLit(randChar).setType(TChar)
  val string = () => StringLit(Rand.nextString(StringLength)).setType(TString)

  val types        = List[() => ExprTree](int, bool, long, float, double, char, string)
  val combinations = for (x <- types; y <- types) yield (x, y)



  implicit def intWithTimes(n: Int) = new {
    def times(f: => Unit): Unit = 1 to n foreach { _ => f }
  }


  before {
    testFolderFile.mkdir()
    Symbols.ID.reset()
  }

  after {
    testFile.delete()
    testFolderFile.listFiles.foreach(_.delete())
    testFolderFile.delete()
  }

  behavior of s"RandomTesting ($NumberOfTests x)"

  it should "Plus" in testOperator(Plus)
  it should "Minus" in testOperator(Minus)
  it should "Times" in testOperator(Times)
  it should "Div" in testOperator(Div)
  it should "Mod" in testOperator(Modulo)

  it should "LogicAnd" in testOperator(LogicAnd)
  it should "LogicOr" in testOperator(LogicOr)
  it should "LogicXor" in testOperator(LogicXor)

  it should "LeftShift" in testOperator(LeftShift)
  it should "RightShift" in testOperator(RightShift)

  it should "Assign" in testAssignmentOperator(Assign)
  it should "ArrayAssign" in testArrayAssignmentOperator(ArrayAssign)

  it should "LessThan" in testOperator(LessThan)
  it should "LessThanEquals" in testOperator(LessThanEquals)
  it should "GreaterThan" in testOperator(GreaterThan)
  it should "GreaterThanEquals" in testOperator(GreaterThanEquals)

  // TODO: Support String equality
  ignore should "Equals" in testOperator(Equals)
  ignore should "NotEquals" in testOperator(NotEquals)


  def testOperator(operator: (ExprTree, ExprTree) => ExprTree) = {
    combinations.foreach { case (lhs, rhs) =>
      val expr = operator(lhs(), rhs())

      if (exprTypeChecks(expr)) {
        NumberOfTests times testExpression(expr)
      }
    }
  }

  def testAssignmentOperator(operator: (Identifier, ExprTree) => ExprTree) =
    combinations.foreach { case (lhs, rhs) =>
      val tpe = lhs().getType
      val id = Identifier(IdName).setSymbol(new VariableSymbol(IdName)).setType(tpe)
      id.setType(tpe)
      def expr = () => operator(id, rhs())
      if (exprTypeChecks(expr())) {
        NumberOfTests times testAssignment(tpe, expr())
      }
    }

   def testArrayAssignmentOperator(operator: (Identifier, ExprTree, ExprTree) => ExprTree) =
    combinations.foreach { case (lhs, rhs) =>
      val tpe = lhs().getType
      val id = Identifier(IdName).setSymbol(new VariableSymbol(IdName)).setType(TArray(tpe))
      def expr = () => operator(id, IntLit(0), rhs())
      println(tpe + ",  " + rhs().getType)
      if (exprTypeChecks(expr())) {
        NumberOfTests times testArrayAssignment(tpe, expr())
      }
    }

  private def scalaVariableDeclaration(tpe: Type) = {
    val scalaType = tpe match {
      case TBool => "Boolean"
      case _     => tpe.toString
    }

    val defaultValue = tpe match {
      case TInt | TLong | TFloat | TDouble | TChar => "0"
      case TBool                                   => "false"
      case TString                                 => "\"\""
      case _ => ???
    }
    s"var $IdName: $scalaType = $defaultValue"
  }

  private def scalaArrayDeclaration(tpe: Type) = {
    val scalaType = tpe match {
      case TBool => "Boolean"
      case _     => tpe.toString
    }

    s"var $IdName: Array[$scalaType] = new Array[$scalaType](1)"
  }

  private def exprTypeChecks(expr: ExprTree) = TypeChecker.tcExpr(expr) match {
    case TError                               => false
    case _ if TypeCheckCtx.reporter.hasErrors =>
      TypeCheckCtx.reporter.clear()
      false
    case _                                    =>
      true
  }

  private def testExpression(expr: ExprTree) = {
    val operation = Printer(expr)
    println("Testing " + operation)
    val scalaRes = getScalaResult(operation)
    val res = getResult(operatorProgram(operation))
    assert(res == scalaRes, s" for expression $operation.\n\tScala:  '$scalaRes'\n\tResult: '$res'")
  }

  private def testAssignment(tpe: Type, expr: ExprTree) = {
    TestUtils.Interpreter.interpret(scalaVariableDeclaration(tpe))
    val operation = Printer(expr)
    println(s"Testing $operation ($IdName : $tpe)")
    val scalaRes = getScalaResult(operation).trim + System.lineSeparator() + getScalaResult(IdName).trim
    if (!scalaRes.contains("error")) {
      val res = getResult(assignmentProgram(operation, tpe)).trim
      assertResult(operation + " (x: " + tpe + ")", res, scalaRes)
    }

  }

  private def testArrayAssignment(tpe: Type, expr: ExprTree) = {
    TestUtils.Interpreter.interpret(scalaArrayDeclaration(tpe))
    val operation = Printer(expr)
    println(s"Testing $operation ($IdName : $tpe[])")
    val scalaRes = getScalaResult(IdName + "(0)")
    if (!scalaRes.contains("error")) {
      val res = getResult(arrayAssignmentProgram(operation, tpe))
      assertResult(operation + " (x: " + tpe + ")", res, scalaRes)
    }
  }

  private def assertResult(operation: String, res: String, scalaRes: String) =
    assert(res == scalaRes, s" for expression $operation.\n\tScala:  '$scalaRes'\n\tResult: '$res'")

  private def operatorProgram(operation: String) = s"println($operation)"

  private def assignmentProgram(operation: String, tpe: Type) =
    s"""
var $IdName: $tpe
println($operation)
println($IdName)
     """

  private def arrayAssignmentProgram(operation: String, tpe: Type) =
    s"""
var $IdName: $tpe[] = new $tpe[1]
println($operation)
println($IdName[0])
     """


  private def getResult(program: String) = {
    setTestProgram(program)
    Compiler.run(TestCtx)(TestCtx.file)
    val mainName = TestCtx.file.getName.dropRight(Main.FileEnding.length)
    TestUtils.executeTProgram(TestFolder, mainName).trim
  }

  private def getScalaResult(operation: String) = {
    val r = TestUtils.Interpreter.interpret(operation)
    if (r.contains("error")) "error"
    else r.split("=").last.trim
  }

  private def setTestProgram(program: String) = {
    var out: FileWriter = null
    try {
      out = new FileWriter(TestFilePath)
      out.write(program)
    } catch {
      case e: IOException => e.printStackTrace()
    } finally {
      if (out != null)
        out.close()
    }
  }

  private def randChar = {
    val illegalChars = List[Char]('\'')
    var c: Char = 0
    do{
      c = Rand.nextPrintableChar()
    }while(illegalChars.contains(c))
    c
  }
}
