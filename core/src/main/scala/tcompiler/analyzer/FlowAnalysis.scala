package tcompiler.analyzer

import tcompiler.analyzer.Symbols.{ClassSymbol, FieldSymbol, VariableSymbol}
import tcompiler.analyzer.Types._
import tcompiler.ast.TreeTraverser
import tcompiler.ast.Trees._
import tcompiler.imports.ImportMap
import tcompiler.utils.{Context, Pipeline, Positioned}
import tcompiler.utils.Extensions._

import scala.collection.mutable.ListBuffer
import scala.collection.mutable
import scala.reflect._

/**
  * Created by timlindeberg on 22/07/16.
  */
object FlowAnalysis extends Pipeline[List[CompilationUnit], List[CompilationUnit]] {

  override def run(ctx: Context)(cus: List[CompilationUnit]): List[CompilationUnit] = {
    cus foreach { cu =>
      cu.classes foreach { clazz =>
        val flowAnalyser = new FlowAnalyser(ctx, cu.importMap)
        flowAnalyser(clazz)
      }
    }
    cus
  }

}

class FlowAnalyser(override var ctx: Context, override var importMap: ImportMap) extends FlowAnalysisErrors {

  trait Identifier extends Typed {
    def symbol: Option[VariableSymbol]

    def baseIdentiferEquals(varId: Identifier): Boolean
  }

  case class VarIdentifier(varSymbol: VariableSymbol) extends Identifier {
    val symbol = Some(varSymbol)
    def baseIdentiferEquals(varId: Identifier) = varId match {
      case VarIdentifier(v) => varSymbol == v
      case _                => false
    }
    override val getType = varSymbol.getType
    override def toString = varSymbol.name
  }

  case class ClassIdentifier(classSymbol: ClassSymbol) extends Identifier {
    val symbol = None
    def baseIdentiferEquals(varId: Identifier) = varId match {
      case ClassIdentifier(c) => classSymbol == c
      case _                  => false
    }
    override val getType = classSymbol.getType
    override def toString = classSymbol.name
  }

  case class AccessIdentifier(id: Identifier, varSymbol: VariableSymbol) extends Identifier {
    val symbol = Some(varSymbol)
    def baseIdentiferEquals(varId: Identifier): Boolean = {
      if (id == varId) true
      else id.baseIdentiferEquals(varId)
    }

    override val getType = varSymbol.getType
    override def toString = s"$id.${varSymbol.name}"
  }

  case class ArrayItemIdentifier(id: Identifier, index: Int) extends Identifier {
    val symbol = None
    def baseIdentiferEquals(varId: Identifier) = {
      if (id == varId) true
      else id.baseIdentiferEquals(varId)
    }

    override val getType = id.getType.asInstanceOf[TArray].tpe

    override def toString = s"$id[$index]"
  }


  trait VarKnowledge {
    def invert: VarKnowledge = this
  }

  trait VarKnowledgeWrapper extends VarKnowledge {
    def inner: VarKnowledge
  }

  // Used to wrap knowledge to show it came from an and or an or expression
  case class OrKnowledge(inner: VarKnowledge) extends VarKnowledgeWrapper {override def invert = AndKnowledge(inner.invert)}
  case class AndKnowledge(inner: VarKnowledge) extends VarKnowledgeWrapper {override def invert = OrKnowledge(inner.invert)}

  // These should be objects but then it doesnt work as type parameters
  class Initialized extends VarKnowledge {override def toString = "Initialized"}
  class Used extends VarKnowledge {override def toString = "Used"}
  val Initialized = new Initialized
  val Used        = new Used

  case class Reassigned(at: StatTree) extends VarKnowledge {override def toString = s"Reassigned(${at.line}:${at.col})"}
  case class IsNull(value: Boolean) extends VarKnowledge {override def invert = IsNull(!value)}
  case class ArraySize(size: Int) extends VarKnowledge
  case class NumericValue(value: Int) extends VarKnowledge
  case class BoolValue(condition: ExprTree) extends VarKnowledge

  // TODO: Ranges?
  case class Greater(value: Int) extends VarKnowledge {
    override def invert = LessEq(value)
  }
  case class GreaterEq(value: Int) extends VarKnowledge {
    override def invert = Less(value)
  }
  case class Less(value: Int) extends VarKnowledge {
    override def invert = GreaterEq(value)
  }
  case class LessEq(value: Int) extends VarKnowledge {
    override def invert = Greater(value)
  }

  case class Knowledge(varKnowledge: Map[Identifier, Set[VarKnowledge]] = Map(),
                       flowEnded: Option[StatTree] = None) {

    def +(other: Knowledge) = {
      val knowledge = mutable.Map[Identifier, Set[VarKnowledge]]()
      List(this, other) foreach {
        _.varKnowledge foreach { case (id, knowledge1) =>
          val v = knowledge.getOrElse(id, Set())
          knowledge += id -> (v ++ knowledge1)
        }
      }
      val flow = if (other.flowEnded.isDefined) other.flowEnded else flowEnded
      new Knowledge(knowledge.toMap, flow)
    }

    def -(other: Knowledge) = {
      val knowledge = mutable.Map[Identifier, Set[VarKnowledge]]()
      varKnowledge.foreach { case (id, knowledge1) =>
        other.varKnowledge.get(id) match {
          case Some(knowledge2) =>
            val diff = knowledge1.diff(knowledge2)
            if (diff.nonEmpty)
              knowledge += id -> diff
          case None             => knowledge += id -> knowledge1
        }
      }
      val flow = (flowEnded, other.flowEnded) match {
        case (Some(f1), Some(f2)) if f1 == f2 => None
        case _                                => flowEnded
      }
      new Knowledge(knowledge.toMap, flow)
    }

    def intersection(other: Knowledge, newFlowEnded: Option[StatTree]) = {
      val knowledge = mutable.Map[Identifier, Set[VarKnowledge]]()
      varKnowledge.foreach { case (id, knowledge1) =>
        other.varKnowledge.get(id) ifDefined { knowledge2 =>
          knowledge += id -> knowledge1.intersect(knowledge2)
        }
      }

      val flow = newFlowEnded match {
        case Some(f) => if (flowEnded.isDefined && other.flowEnded.isDefined) Some(f) else None
        case None    => None
      }
      new Knowledge(knowledge.toMap, flow)
    }

    def add[T <: VarKnowledge : ClassTag](varId: Identifier, newKnowledge: T): Knowledge = {
      varId.symbol ifDefined { sym =>
        if (sym.isInstanceOf[FieldSymbol] && !sym.isFinal) {
          // cannot gain knowledge about var fields since they can change at any time
          return this
        }
      }
      val knowledge = varKnowledge.getOrElse(varId, Set()).filterNotType[T] + newKnowledge
      copy(varKnowledge = varKnowledge + (varId -> knowledge))
    }


    def get[T <: VarKnowledge : ClassTag](varId: Identifier): Option[T] = {
      val v = varKnowledge.getOrElse(varId, Set())
      v.findInstance[T] match {
        case Some(x) => Some(x)
        case None    => v.findInstance[AndKnowledge] flatMap {
          case AndKnowledge(x) =>
            if (classTag[T].runtimeClass.isInstance(x))
              Some(x.asInstanceOf[T])
            else
              None
          case _                  => None
        }
      }
    }

    def invert = copy(varKnowledge = {
      val knowledge = mutable.Map[Identifier, Set[VarKnowledge]]()
      varKnowledge foreach { case (varId, k) =>
        knowledge += varId -> k.map(_.invert)
      }
      knowledge.toMap
    })

    def assignment(varId: Identifier, init: Option[ExprTree], extraInfo: VarKnowledge*) = {
      val varTpe = varId.getType
      val initial = getInitialKnowledge(varId, varTpe, init)
      // Remove old knowledge of the given variable symbol
      val newKnowledge = filterOldKnowledge(varId) ++ initial
      val varIdKnowledge = newKnowledge(varId) ++ extraInfo.toSet
      copy(varKnowledge = newKnowledge + (varId -> varIdKnowledge))
    }

    def addToNumericValue(varId: Identifier, increment: Int) = {
      get[NumericValue](varId) match {
        case Some(NumericValue(v)) => setNumericValue(varId, v + increment)
        case _                     => this
      }
    }

    def setNumericValue(varId: Identifier, value: Int) = {
      val numericKnowledge = NumericValue(value)
      val oldKnowledge = varKnowledge.getOrElse(varId, Set()).filterNotType[NumericValue]
      val newKnowledge = oldKnowledge + numericKnowledge
      copy(varKnowledge = varKnowledge + (varId -> newKnowledge))
    }


    def endFlow(at: StatTree) = copy(flowEnded = Some(at))

    def addOrKnowledge(from: Knowledge) = _addWrapped(from, OrKnowledge)
    def addAndKnowledge(from: Knowledge) = _addWrapped(from, AndKnowledge)

    def filterReassignedVariables(branch: StatTree, afterBranch: Knowledge) = {
      val gainedKnowledge = afterBranch - this
      val newKnowledge = mutable.Map[Identifier, Set[VarKnowledge]]() ++ varKnowledge
      gainedKnowledge.varKnowledge foreach { case entry@(varId, knowledge) =>
        knowledge.findInstance[Reassigned] match {
          case Some(Reassigned(_)) =>
            // Variable could have gotten reassigned in the branch so we have to
            // remove previous knowledge
            // The knowledge that can be saved from the branch is the intersection
            // of the knowledge after the branch and the original knowledge
            val inter = intersection(afterBranch, None)
            val varIdKnowledge = inter.varKnowledge.getOrElse(varId, Set())
            inter.varKnowledge foreach { case (vId, k) => newKnowledge += vId -> k }
            newKnowledge += varId -> (varIdKnowledge + Reassigned(branch))
          case None                =>
        }
      }
      copy(varKnowledge = newKnowledge.toMap)
    }

    def getIdentifier(obj: ExprTree): Option[Identifier] =
      obj match {
        case v: VariableID      => Some(new VarIdentifier(v.getSymbol))
        case c: ClassID         => Some(new ClassIdentifier(c.getSymbol))
        case Assign(to, from)   => getIdentifier(to)
        case access: Access     => getAccessIdentifier(access)
        case arrRead: ArrayRead => getArrayIdentifier(arrRead)
        case _                  => None
      }


    def getNumericValue(expr: ExprTree): Option[Int] = {
      expr match {
        case IntLit(value)                   => Some(value)
        case assignable: Assignable          => getIdentifier(assignable) flatMap { id => get[NumericValue](id).map(_.value) }
        case op@BinaryOperatorTree(lhs, rhs) =>
          getNumericValue(lhs) flatMap { lhsValue =>
            getNumericValue(rhs) map { rhsValue =>
              val operator = getBinaryOperator(op)
              operator(lhsValue, rhsValue)
            }
          }
        case op@UnaryOperatorTree(expr)      =>
          getNumericValue(expr) flatMap { exprValue =>
            getUnaryOperator(op) map { operator =>
              // we currently cannot infer knowledge when increment and decrement is used
              operator(exprValue)
            }
          }
        case _                               => None
      }

    }

    private def _addWrapped[T <: VarKnowledgeWrapper : ClassTag](from: Knowledge, cons: VarKnowledge => T) = {
      val newKnowledge = mutable.Map[Identifier, Set[VarKnowledge]]()
      from.varKnowledge.foreach { case (varId, vKnowledge) =>
        val v = varKnowledge.getOrElse(varId, Set())
        val wrapped = vKnowledge map {
          case k: VarKnowledgeWrapper => k
          case k if v.contains(k)     => k
          case k                      => cons(k)
        }
        newKnowledge += varId -> (v ++ wrapped)
      }
      copy(varKnowledge = varKnowledge ++ newKnowledge)
    }

    private def getAccessIdentifier(access: Access): Option[Identifier] = {
      access match {
        case Access(obj, _: MethodCall)  => None
        case Access(obj, id: VariableID) => getIdentifier(obj) match {
          case Some(objId) => Some(new AccessIdentifier(objId, id.getSymbol))
          case None        => None
        }
        case _                           => None
      }
    }

    private def getArrayIdentifier(arrRead: ArrayRead): Option[Identifier] = {
      val arr = arrRead.arr
      if (!arr.getType.isInstanceOf[TArray])
        return None

      getIdentifier(arr) flatMap { id =>
        val index = arrRead.index
        getNumericValue(index) flatMap { value =>
          Some(new ArrayItemIdentifier(id, value))
        }
      }
    }

    private def getInitialKnowledge(varId: Identifier, varTpe: Type, maybeInit: Option[ExprTree]): List[(Identifier, Set[VarKnowledge])] = {
      if (maybeInit.isEmpty) {
        val s: Set[VarKnowledge] = if (varTpe.isInstanceOf[PrimitiveType]) Set(Initialized) else Set()
        return List(varId -> s)
      }
      val init = maybeInit.get
      val knowledge = ListBuffer[(Identifier, Set[VarKnowledge])]()
      var varIdKnowledge = Set[VarKnowledge](Initialized)

      // Old knowledge gets transferred to new var
      getIdentifier(init) ifDefined { initId =>
        if (varKnowledge.contains(initId)) {
          varKnowledge.getOrElse(initId, Set()) foreach { k => varIdKnowledge += k }
          knowledge += varId -> varIdKnowledge
          return knowledge.toList
        }
      }

      if (varTpe.isNullable) {
        init.getType match {
          case TNull              => varIdKnowledge += IsNull(true)
          case t if !t.isNullable => varIdKnowledge += IsNull(false)
          case _                  =>
        }
      }

      varTpe match {
        case TArray(arrTpe) =>
          init match {
            case ArrayLit(value)             =>
              value.zipWithIndex.foreach { case (init, index) =>
                val arrayVarId = new ArrayItemIdentifier(varId, index)
                knowledge ++= getInitialKnowledge(arrayVarId, arrTpe, Some(init))
              }
              varIdKnowledge += ArraySize(value.size)
            case NewArray(tpe, size :: rest) =>
              getNumericValue(size) ifDefined { value => varIdKnowledge += ArraySize(value) }
            case _                           =>
          }
        case _: TInt        => getNumericValue(init) ifDefined { value => varIdKnowledge += NumericValue(value) }
        case _: TBool       => varIdKnowledge += BoolValue(init)
        case _              =>
      }

      knowledge += varId -> varIdKnowledge
      knowledge.toList
    }

    private def filterOldKnowledge(varId: Identifier) =
      varKnowledge.filter { case (id, _) => !id.baseIdentiferEquals(varId) }

    private def getUnaryOperator(unaryOperatorTree: UnaryOperatorTree): Option[Int => Int] = unaryOperatorTree match {
      case _: Hash                   => Some(_.hashCode)
      case _: Negation               => Some(-_)
      case _: LogicNot               => Some(~_)
      case _: IncrementDecrementTree => None
    }

    private def getBinaryOperator(binaryOperatorTree: BinaryOperatorTree): (Int, Int) => Int = binaryOperatorTree match {
      case _: Plus       => _ + _
      case _: Minus      => _ - _
      case _: Times      => _ * _
      case _: Div        => _ / _
      case _: Modulo     => _ % _
      case _: LogicAnd   => _ & _
      case _: LogicOr    => _ | _
      case _: LogicXor   => _ ^ _
      case _: LeftShift  => _ << _
      case _: RightShift => _ >> _
    }

    override def toString = {
      val vars = varKnowledge.map { case (expr, knowledge) =>
        s"$expr -> { ${knowledge.mkString(", ")} }"
      }.mkString("\n")

      val flow = flowEnded match {
        case Some(ended) => s"\nFlow ended at ${ended.line}: $ended"
        case None        => ""
      }
      vars + flow
    }
  }


  def apply(clazz: ClassDeclTree): Unit = {

    val fieldKnowledge = clazz.fields.foldLeft(new Knowledge()) { (knowledge, field) =>
      val sym = field.getSymbol
      val varId = new VarIdentifier(sym)
      if (sym.modifiers.contains(Final()))
        knowledge.assignment(varId, field.init, Initialized)
      else
        knowledge.add(varId, Initialized)
    }

    clazz.methods.filter(_.stat.isDefined) foreach { meth =>
      val args = meth.getSymbol.argList
      val argMap: Map[Identifier, Set[VarKnowledge]] =
        args.map { v => new VarIdentifier(v) -> Set[VarKnowledge](Initialized) }.toMap
      val argKnowledge = new Knowledge(argMap)
      val knowledge: Knowledge = fieldKnowledge + argKnowledge
      analyze(meth.stat.get, knowledge)
    }

  }

  def analyze(tree: StatTree, knowledge: Knowledge): Knowledge = {
    //println(s"${tree.line}: $tree")
    //println(knowledge)
    //println("----------------------------------------")
    tree match {
      case Block(stats)                      =>
        val endKnowledge = stats.foldLeft(knowledge)((currentKnowledge, next) => analyze(next, currentKnowledge))
        endKnowledge.flowEnded match {
          case Some(stat) if stat != stats.last =>
            val index = stats.indexOf(stat)
            val startStat = stats(index + 1)
            val endStat = stats.last
            WarningDeadCode(startStat.line, endStat.line, startStat)
          case _                                =>
        }
        endKnowledge
      case varDecl@VarDecl(_, id, init, _)   =>
        val varId = new VarIdentifier(id.getSymbol)
        init match {
          case Some(i) =>
            val afterInit = analyzeExpr(i, knowledge)
            afterInit.assignment(varId, init)
          case None    => knowledge.assignment(varId, None)
        }
      case For(init, condition, post, stat)  =>
        val afterInit = init.foldLeft(knowledge)((currentKnowledge, next) => analyze(next, currentKnowledge))
        val afterCondition = analyzeCondition(condition, afterInit)
        val afterStat = analyze(stat, afterCondition)

        val afterPost = post.foldLeft(afterStat)((knowledge, p) => analyze(p, knowledge))
        knowledge.filterReassignedVariables(tree, afterPost)
      case Foreach(varDecl, container, stat) =>
        analyzeExpr(container, knowledge)
        checkValidUse(container, knowledge)
        val varId = new VarIdentifier(varDecl.id.getSymbol)
        val varDeclKnowledge = knowledge.assignment(varId, None, Initialized)
        val afterStat = analyze(stat, varDeclKnowledge)
        knowledge.filterReassignedVariables(tree, afterStat)
      case If(condition, thn, els)           =>
        val afterCondition = analyzeCondition(condition, knowledge)
        val conditionKnowledge = afterCondition - knowledge
        val invertedCondition = knowledge + conditionKnowledge.invert

        val thnKnowledge = analyze(thn, afterCondition)
        val elsKnowledge = els map { e => analyze(e, invertedCondition) }
        val thnEnded = thnKnowledge.flowEnded.isDefined

        elsKnowledge match {
          case Some(elsKnowledge) =>
            val elsEnded = elsKnowledge.flowEnded.isDefined
            if (thnEnded)
              WarningUnnecessaryElse(els.get)

            if (elsEnded && !thnEnded)
              thnKnowledge // we know the then branch has executed
            else if (!elsEnded && thnEnded)
              elsKnowledge // we know the else branch has executed
            else if (elsEnded && thnEnded)
              knowledge.endFlow(tree) // execution is dead after if branch
            else {
              val intersect = thnKnowledge.intersection(elsKnowledge, Some(tree))
              intersect.filterReassignedVariables(thn, thnKnowledge).filterReassignedVariables(els.get, elsKnowledge)
            }
          case None               =>
            if (thnEnded)
              invertedCondition
            else
              knowledge.filterReassignedVariables(tree, thnKnowledge)
        }
      case While(condition, stat)            =>
        val afterCondition = analyzeCondition(condition, knowledge)
        val afterWhile = analyze(stat, afterCondition)
        knowledge.filterReassignedVariables(tree, afterWhile)
      case PrintStatTree(expr)               =>
        analyzeExpr(expr, knowledge)
      case Error(expr)                       =>
        analyzeExpr(expr, knowledge)
        knowledge.endFlow(tree)
      case Return(expr)                      =>
        expr ifDefined {analyzeExpr(_, knowledge)}
        knowledge.endFlow(tree)
      case _: Break | _: Continue            =>
        knowledge.endFlow(tree)
      case exprTree: ExprTree                =>
        analyzeExpr(exprTree, knowledge)
    }
  }

  def analyzeCondition(tree: ExprTree, knowledge: Knowledge): Knowledge = tree match {
    case And(lhs, rhs)                =>
      val lhsKnowledge = analyzeCondition(lhs, knowledge)
      val rhsKnowledge = analyzeCondition(rhs, lhsKnowledge)
      val conditionKnowledge = rhsKnowledge - knowledge
      knowledge.addAndKnowledge(conditionKnowledge)
    case Or(lhs, rhs)                 =>
      val lhsKnowledge = analyzeCondition(lhs, knowledge)
      val rhsKnowledge = analyzeCondition(rhs, lhsKnowledge)
      val conditionKnowledge = rhsKnowledge - knowledge
      knowledge.addOrKnowledge(conditionKnowledge)
    case Not(expr)                    =>
      // Apply De Morgans law:
      expr match {
        case And(lhs, rhs) =>
          analyzeCondition(Or(Not(lhs), Not(rhs)), knowledge)
        case Or(lhs, rhs)  =>
          analyzeCondition(And(Not(lhs), Not(rhs)), knowledge)
        case _             =>
          getBoolVarCondition(expr, knowledge) match {
            case Some(condition) => analyzeCondition(Not(condition), knowledge)
            case None            =>
              val exprKnowledge = analyzeCondition(expr, knowledge)
              knowledge + (exprKnowledge - knowledge).invert
          }
      }
    case assignable: Assignable       =>
      analyzeExpr(assignable, knowledge)
      getBoolVarCondition(assignable, knowledge) match {
        case Some(condition) =>
          analyzeCondition(condition, knowledge)
        case None            =>
          // Not a bool var
          nullCheck(tree, assignable, knowledge, isNull = false)
      }
    case EqualsOperatorTree(lhs, rhs) =>
      val isNull = tree.isInstanceOf[Equals]
      val lhsKnowledge = analyzeExpr(lhs, knowledge)
      val rhsKnowledge = analyzeExpr(rhs, lhsKnowledge)

      // Returns the value which
      def getOther(exprTree: ExprTree): Option[ExprTree] = {
        val l = List(lhs, rhs)
        if (!l.contains(exprTree))
          return None
        l.find(_ != exprTree)
      }

      getOther(NullLit()) ifDefined { x => return nullCheck(tree, x, rhsKnowledge, isNull) }

      getOther(TrueLit()) ifDefined { x =>
        getBoolVarCondition(x, rhsKnowledge) ifDefined { condition =>
          return analyzeCondition(condition, rhsKnowledge)
        }
      }

      getOther(FalseLit()) ifDefined { x =>
        getBoolVarCondition(x, rhsKnowledge) ifDefined { condition =>
          return analyzeCondition(Not(condition), rhsKnowledge)
        }
      }

      rhsKnowledge
    case _                            =>
      analyzeExpr(tree, knowledge)
  }

  def analyzeExpr(tree: ExprTree, topKnowledge: Knowledge): Knowledge = {
    var knowledge = topKnowledge
    val traverser = new TreeTraverser {
      override def traverse(t: Tree) = t match {
        case Ternary(condition, thn, els)       =>
          val afterCondition = analyzeCondition(condition, knowledge)
          val conditionKnowledge = afterCondition - knowledge

          analyzeExpr(thn, afterCondition)
          analyzeExpr(els, knowledge + conditionKnowledge.invert)
        case acc@Access(obj, app)               =>
          traverse(obj, app)
          checkValidUse(obj, knowledge)
        case assign@Assign(obj, from)           =>
          super.traverse(t)

          knowledge.getIdentifier(obj) ifDefined { varId =>
            // Reset knowledge
            checkReassignment(varId, t)
            knowledge = knowledge.assignment(varId, Some(from), Reassigned(assign))
          }
        case binOp@BinaryOperatorTree(lhs, rhs) =>
          traverse(lhs, rhs)
          binOp ifInstanceOf[Div] { div =>
            knowledge.getNumericValue(rhs) ifDefined { v => if (v == 0) ErrorDivideByZero(rhs, binOp) }
          }
          binOp match {
            case _: EqualsOperatorTree | _: And | _: Or =>
            case _                                      =>
              // Rest of the binary operators need to be null checked before use
              checkValidUse(lhs, knowledge)
              checkValidUse(rhs, knowledge)
          }
        case ExtractNullable(expr)              =>
          traverse(expr)
        case op@UnaryOperatorTree(expr)         =>
          traverse(expr)
          checkValidUse(expr, knowledge)
          op.ifInstanceOf[IncrementDecrementTree]({ incDec =>
            knowledge.getIdentifier(expr) ifDefined { varId =>
              checkReassignment(varId, t)
              val v = if (incDec.isIncrement) 1 else -1
              knowledge = knowledge.addToNumericValue(varId, v)
            }
          })
        case ArrayOperatorTree(arr)             =>
          traverse(arr)

          t match {
            case arrRead@ArrayRead(_, index) =>
              knowledge.getNumericValue(index) ifDefined { value =>
                if (value < 0)
                  ErrorOutOfBounds(index, value, 0, arrRead)
                else
                  getArraySize(arr, knowledge) ifDefined { size =>
                    if (value >= size)
                      ErrorOutOfBounds(index, value, size, arrRead)
                  }
              }
            case _                           =>
          }
          checkValidUse(arr, knowledge)
        case _                                  =>
          super.traverse(t)
      }
    }
    traverser.traverse(tree)
    knowledge
  }

  private def checkReassignment(varId: Identifier, pos: Positioned) =
    varId.symbol ifDefined { sym =>
      if (sym.modifiers.contains(Final()))
        ErrorReassignmentToVal(sym.name, pos)
    }

  private def getBoolVarCondition(obj: ExprTree, knowledge: Knowledge) =
    knowledge.getIdentifier(obj) flatMap { id => knowledge.get[BoolValue](id).map(_.condition) }

  private def getArraySize(obj: ExprTree, knowledge: Knowledge) =
    knowledge.getIdentifier(obj) flatMap { id => knowledge.get[ArraySize](id).map(_.size) }

  private def nullCheck(t: Tree, obj: ExprTree, knowledge: Knowledge, isNull: Boolean) =
    knowledge.getIdentifier(obj) match {
      case Some(varId) =>
        knowledge.get[IsNull](varId) match {
          case Some(IsNull(knownNull)) =>
            val known = if (knownNull) "to be 'null'" else "not to be 'null'"
            WarningUnnecessaryCheck(obj, known, t)
            knowledge
          case None                    => knowledge.add(varId, IsNull(isNull))
        }
      case None        => knowledge
    }

  private def checkValidUse(obj: ExprTree, knowledge: Knowledge): Unit = {
    if (obj.isInstanceOf[ClassID])
      return

    val objTpe = obj.getType
    obj match {
      case NormalAccess(_, MethodCall(meth, _)) if meth.getType.isNullable =>
        if (objTpe.isNullable)
          ErrorAccessNullableMethod(meth.getSymbol.signature, obj)
      case _                                                               =>
        knowledge.getIdentifier(obj) match {
          case Some(varId) =>
            if (objTpe.isNullable)
              knowledge.get[IsNull](varId) match {
                case Some(IsNull(isNull)) if isNull => ErrorAccessIsNull(obj, obj)
                case None                           => ErrorAccessMightBeNull(obj, obj)
                case _                              =>
              }
            varId.symbol ifDefined { varSym =>
              if (!varSym.isInstanceOf[FieldSymbol] && knowledge.get[Initialized](varId).isEmpty)
                ErrorVariableNotInitialized(obj, obj)
            }
          case _           =>
        }
    }
  }

}