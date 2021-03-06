package tlang
package compiler
package analyzer

import tlang.compiler.messages.{ErrorHandling, ErrorMessage, ExtraMessage, WarningMessage}
import tlang.utils.Positioned

import scala.collection.immutable.NumericRange

trait FlowingErrors extends ErrorHandling {

  def report(error: ErrorMessage): Unit = reporter.report(error)

  //---------------------------------------------------------------------------------------
  //  Error messages
  //---------------------------------------------------------------------------------------

  import errorStringContext._

  val ErrorLetters = "F"
  abstract class FlowAnalysisError(code: Int, pos: Positioned) extends ErrorMessage(ErrorLetters, code, pos)
  abstract class FlowAnalysisWarning(code: Int, pos: Positioned) extends WarningMessage(ErrorLetters, code, pos)

  case class AccessMightBeNull(v: String, override val pos: Positioned) extends FlowAnalysisError(0, pos) {
    lazy val message = err"Cannot use nullable value $v without first checking if it is null."
  }

  case class AccessIsNull(v: String, override val pos: Positioned) extends FlowAnalysisError(1, pos) {
    lazy val message = err"Cannot use nullable value $v since it is known to be null."
  }

  case class AccessNullableMethod(meth: String, override val pos: Positioned) extends FlowAnalysisError(2, pos) {
    lazy val message = err"Cannot directly use result of method call $meth since it could be null."
  }

  case class DivideByZero(zeroExpr: String, override val pos: Positioned) extends FlowAnalysisError(3, pos) {
    lazy val message = err"Division by expression $zeroExpr is illegal since it is known to have the value ${ 0 }."
  }

  object OutOfBounds {
    def apply(index: String, value: Long, size: Long, pos: Positioned): OutOfBounds = {
      val bounds = if (value < 0) s"$value < 0" else s"$value > $size"
      OutOfBounds(index, err"$bounds", pos)
    }
    def apply(index: String, range: NumericRange[Long], size: Long, pos: Positioned): OutOfBounds = {
      val bounds = err"size $size is not in range ${ range.min }..${ range.max }"
      OutOfBounds(index, bounds, pos)
    }
  }

  case class OutOfBounds(index: String, bounds: String, override val pos: Positioned) extends FlowAnalysisError(4, pos) {
    lazy val message: String = err"Indexing expression $index is out of bounds: " + bounds
  }

  case class ReassignmentToVal(value: String, override val pos: Positioned) extends FlowAnalysisError(5, pos) {
    lazy val message = err"Cannot reassign value $value."
  }

  case class VariableNotInitialized(v: String, override val pos: Positioned) extends FlowAnalysisError(6, pos) {
    lazy val message = err"Cannot use variable $v since it may not have been initialized."
  }

  case class NotAllPathsReturnAValue(override val pos: Positioned) extends FlowAnalysisError(7, pos) {
    lazy val message = err"Not all code paths return a value."
  }

  case class FieldMayNotHaveBeenInitialized(field: String, fieldPos: Positioned, clazz: String, constructor: String, override val pos: Positioned) extends FlowAnalysisError(8, pos) {
    lazy val message = err"Field $field may not have been initialized in constructor $clazz::$constructor."

    case class VarDeclExtraMessage() extends ExtraMessage(fieldPos) {
      lazy val message: String = err"$field is declared here:"
    }

    override lazy val notes = List(VarDeclExtraMessage())
  }

  //---------------------------------------------------------------------------------------
  //  Warnings
  //---------------------------------------------------------------------------------------

  case class DeadCode(startLine: Int, endLine: Int, override val pos: Positioned) extends FlowAnalysisWarning(0, pos) {
    lazy val message: String = {
      val line = if (startLine == endLine) err"line $startLine" else err"lines $startLine - $endLine"
      err"Code on " + line + err" is unreachable."
    }
  }

  case class UnnecessaryCheck(value: String, knownNull: Boolean, override val pos: Positioned) extends FlowAnalysisWarning(1, pos) {
    lazy val message: String = {
      val known = if (knownNull) err"to be ${ "null" }" else err"not to be ${ "null" }"
      err"Check is unnecessary, $value is known " + known
    }
  }

  case class UnnecessaryElse(override val pos: Positioned) extends FlowAnalysisWarning(2, pos) {
    lazy val message = err"Else is unnecessary since code flow ends in then branch."
  }
}
