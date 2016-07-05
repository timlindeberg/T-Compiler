package tcompiler.code

import tcompiler.analyzer.Symbols.{ClassSymbol, OperatorSymbol}
import tcompiler.analyzer.Types._
import tcompiler.ast.Trees._
import tcompiler.ast.{TreeTransformer, Trees}
import tcompiler.utils.{Context, Pipeline}

/**
  * Created by Tim Lindeberg on 7/1/2016.
  */
object Desugaring extends Pipeline[List[CompilationUnit], List[CompilationUnit]] {

  override def run(ctx: Context)(cus: List[CompilationUnit]): List[CompilationUnit] = cus map desugar


  def desugar(cu: CompilationUnit) = {
    System.out.flush()
    val desugarer = new Desugarer()
    desugarer(cu)
  }
}
class Desugarer {

  def apply(cu: CompilationUnit) = {

    // Replace operator decls first so all are replaced when we replace
    // operator calls
    val desugarTransformer = new TreeTransformer {

      override def transform(t: Tree) = t match {
        case slice: ArraySlice              => desugarArraySlice(super.transform(slice))
        case opDecl: OperatorDecl           => replaceOperatorDecl(super.transform(opDecl))
        case incDec: IncrementDecrementTree =>
          if (incDec.expr.getType.isInstanceOf[TObject])
            replaceOperatorCall(super.transform(incDec))
          else
            desugarIncrementDecrement(super.transform(incDec))
        case assign: Assign                 =>
          val to = assign.to
          if (to.isInstanceOf[ArrayRead] && to.getType.isInstanceOf[TObject]) {
            val expr = super.transform(assign.expr).asInstanceOf[ExprTree]
            val newAssign = treeCopy.Assign(assign, to, expr)
            replaceOperatorCall(newAssign)
          } else {
            super.transform(assign)
          }
        case op: OperatorTree               => replaceOperatorCall(super.transform(op))
        case foreach: Foreach               => desugarForeachLoop(super.transform(foreach))
        case _                              => super.transform(t)
      }

    }
    val s = desugarTransformer.transform(cu)
    s.asInstanceOf[CompilationUnit]
  }

  /**
    * Replaces overloaded operator calls with calls to static methods
    *
    * a + b => A.$Plus(a, b)
    *
    * or
    *
    * ++a => A.$PreIncrement(a)
    *
    * or
    *
    * a[5] = 5 => a.$ArrayAssign(5, 5)
    */
  def replaceOperatorCall(t: Tree): Tree = {
    if (!t.isInstanceOf[OperatorTree])
      return t

    val op = t.asInstanceOf[OperatorTree]
    val c = new TreeBuilder

    t match {
      case BinaryOperatorTree(lhs, rhs) =>
        if (!(isObject(lhs) || isObject(rhs)))
          return op

        val opSymbol = op.lookupOperator((lhs.getType, rhs.getType)).get
        val obj = getClassID(opSymbol)
        c.createMethodCall(obj, opSymbol, lhs, rhs)
      case UnaryOperatorTree(expr)      =>
        if (!isObject(expr))
          return op

        val opSymbol = op.lookupOperator(expr.getType).get
        val obj = getClassID(opSymbol)
        c.createMethodCall(obj, opSymbol, expr)
      case ArrayOperatorTree(arr)       =>
        if (!isObject(arr))
          return op

        val arrClassSymbol = arr.getType.asInstanceOf[TObject].classSymbol
        op match {
          case ArrayRead(arr, index) =>
            val opSymbol = arrClassSymbol.lookupOperator(op, List(index.getType)).get
            c.createMethodCall(arr, opSymbol, index)
          case Assign(to, expr)      =>
            to match {
              case ArrayRead(arr, index) =>
                val opSymbol = arrClassSymbol.lookupOperator(op, List(index.getType, expr.getType)).get
                c.createMethodCall(arr, opSymbol, index, expr).setType(expr.getType)
              case _                     => op
            }
        }
      case _                            => op
    }
  }

  def isObject(e: ExprTree) = e.getType.isInstanceOf[TObject]
  def getClassID(operatorSymbol: OperatorSymbol) = {
    val classSymbol = operatorSymbol.classSymbol
    new ClassID(classSymbol.name).setSymbol(classSymbol)
  }

  def replaceOperatorDecl(t: Tree): Tree = {
    if (!t.isInstanceOf[OperatorDecl])
      return t
    val op = t.asInstanceOf[OperatorDecl]
    val opSymbol = op.getSymbol.asInstanceOf[OperatorSymbol]
    val methodID = new MethodID(opSymbol.name).setSymbol(opSymbol).setPos(op)
    val methDecl =
      if (op.isAbstract)
        new MethodDecl(op.retType, methodID, op.args, op.stat, op.modifiers)
      else opSymbol.operatorType match {
        case Assign(ArrayRead(_, _), _) =>
          // Convert array assignment so the value is returned
          val indexId = op.args(1).id
          val retType = new TreeBuilder().getTypeTree(indexId.getType)
          val ret = Return(Some(indexId))
          val stats = op.stat.get match {
            case Block(s) => s :+ ret
            case stat     => List(stat, ret)
          }
          opSymbol.setType(indexId.getType)
          new MethodDecl(Some(retType), methodID, op.args, Some(Block(stats)), op.modifiers)
        case _                          =>
          new MethodDecl(op.retType, methodID, op.args, op.stat, op.modifiers)
      }
    methDecl.setSymbol(opSymbol).setPos(op)
  }

  /**
    * Desugars for each loops, either array based or an iterator based.
    */
  private def desugarForeachLoop(t: Tree): Tree = {
    if (!t.isInstanceOf[Foreach])
      return t

    val foreach = t.asInstanceOf[Foreach]
    val container = foreach.container
    val varDecl = foreach.varDecl
    val stat = foreach.stat
    container.getType match {
      case TArray(arrTpe)       => desugarArrayForeachLoop(varDecl, container, stat)
      case TObject(classSymbol) => desugarIteratorForeachLoop(classSymbol, varDecl, container, stat)
      case _                    => ???
    }
  }

  //@formatter:off
  /**
    * Transform increment and decrement expressions on accesses and array reads.
    *
    * Examples:
    * --------------------------------------------------------------------------------
    *
    *   a++
    *
    * becomes:
    *
    *   var $v = a
    *   a = a + 1
    *   $v
    *
    * --------------------------------------------------------------------------------
    *
    *   --a
    *
    * becomes:
    *
    *   a = a - 1
    *   a
    *
    * --------------------------------------------------------------------------------
    *
    *   GetObject().I++
    *
    * becomes:
    *
    *   var $obj = GetObject()
    *   var $v = $tmp.I
    *   var $newV = $v + 1
    *   $tmp.I = $newV
    *   $v
    *
    * --------------------------------------------------------------------------------
    *
    *   --GetArray()[GetIndex()*4]
    *
    * becomes:
    *
    *   var $arr
    *   var $idx = GetIndex()*4
    *   var $v = a[$idx]
    *   var $newV = $v - 1
    *   $arr[$idx] = $newV
    *   newV$x
    *
    * --------------------------------------------------------------------------------
    */
    //@formatter:on
  private def desugarIncrementDecrement(t: Tree): Tree = {
    if (!t.isInstanceOf[IncrementDecrementTree])
      return t

    val incDec = t.asInstanceOf[IncrementDecrementTree]
    val c = new TreeBuilder

    def getPlusOrMinus(value: ExprTree) = {
      val o = c.createOne(value.getType)
      val plusOrMinus = if (incDec.isIncrement) Plus(value, o) else Minus(value, o)
      plusOrMinus.setType(value)
    }

    def putResult(to: ExprTree, from: ExprTree, value: ExprTree) = {
      c.put(Assign(to, from).setType(to))
      c.put(PutValue(value))
      c.setPos(incDec)
      c.getCode
    }

    // Simple case first:
    incDec.expr match {
      case variable: VariableID =>
        val plusOrMinus = getPlusOrMinus(variable)
        val v = if (incDec.isPre) variable
                else c.putVarDecl("v", variable)

        return putResult(variable, plusOrMinus, v)
      case _                    =>
    }

    // Otherwise we have an access or array read
    val (assignTo, value) = incDec.expr match {
      case acc@Access(obj, application)  =>
        obj match {
          case _: Identifier[_] =>
            val v = if (incDec.isPre) acc else c.putVarDecl("v", acc)
            (acc, v)
          case _             =>
            val objId = c.putVarDecl("obj", obj)
            val newAccess = NormalAccess(objId, application).setType(application)
            val v = if (newAccess.isStatic && incDec.isPre) newAccess else c.putVarDecl("v", newAccess)
            (newAccess, v)
        }
      case arrRead@ArrayRead(arr, index) =>
        val arrId = arr match {
          case _: VariableID => arr
          case _             => c.putVarDecl("arr", arr)
        }
        val indexId = index match {
          case _: VariableID | _: Literal[_] => index
          case _                             => c.putVarDecl("idx", index)
        }

        val a = ArrayRead(arrId, indexId).setType(arrRead)
        (a, c.putVarDecl("v", a))
    }

    val plusOrMinus = getPlusOrMinus(value)
    if (incDec.isPre) {
      val newValue = c.putVarDecl("newV", plusOrMinus)
      putResult(assignTo, newValue, newValue)
    } else {
      putResult(assignTo, plusOrMinus, value)
    }
  }

  //@formatter:off
  /**
    * Transforms foreach loop over an array
    *
    * Examples:
    *
    * --------------------------------------------------------------------------------
    *
    *   for(<varDecl> in <array>)
    *     <code>
    *
    * becomes:
    *
    *   val $container = <array>
    *   for(var $i = 0; $i < $container.Size(); i++){
    *     <varDecl> = $container[$i]
    *     <code>
    *   }
    *
    * --------------------------------------------------------------------------------
    */
 //@formatter:on
  private def desugarArrayForeachLoop(varDecl: VarDecl, container: ExprTree, stat: StatTree) = {
    val c = new TreeBuilder
    val indexDecl = c.createVarDecl("i", IntLit(0).setType(TInt))
    val index = indexDecl.id

    val containerId = c.putVarDecl("container", container)

    val sizeCall = c.createMethodCall(container, "Size", TInt)

    val comparison = LessThan(index, sizeCall).setType(TBool).setPos(varDecl)
    val post = Assign(index, Plus(index, IntLit(1)).setType(TInt)).setType(TInt).setPos(varDecl)


    val init = Some(ArrayRead(containerId, index).setType(containerId).setPos(varDecl))
    val valInit = varDecl.copy(init = init).setPos(stat)
    valInit.setSymbol(varDecl.getSymbol).setPos(varDecl)
    val stats = Block(List(valInit, stat))

    c.put(For(List(indexDecl), comparison, List(post), stats).setPos(stat))
    c.getCode
  }

  //@formatter:off
  /**
    * Transforms a foreach loop
    *
    * Examples:
    *
    * --------------------------------------------------------------------------------
    *
    *   for(<varDecl> in <container>)
    *     <code>
    *
    * becomes:
    *
    *   val $it = <container>.Iterator()
    *   while($it.HasNext()){
    *     <varDecl> = $it.Iterator()
    *     <code>
    *   }
    *
    * --------------------------------------------------------------------------------
    */
  //@formatter:on
  private def desugarIteratorForeachLoop(classSymbol: ClassSymbol, varDecl: VarDecl, container: ExprTree, stat: StatTree) = {
    val c = new TreeBuilder

    val iteratorCall = c.createMethodCall(container, classSymbol, "Iterator")
    val iterator = c.putVarDecl("it", iteratorCall)

    val iteratorClass = iteratorCall.getType.asInstanceOf[TObject].classSymbol

    val comparisonCall = c.createMethodCall(iterator, iteratorClass, "HasNext")
    val nextMethodCall = c.createMethodCall(iterator, iteratorClass, "Next")

    val valInit = VarDecl(varDecl.tpe, varDecl.id, Some(nextMethodCall), varDecl.modifiers).setPos(stat)
    valInit.setSymbol(varDecl.getSymbol)
    val stats = Block(List(valInit, stat))

    c.put(While(comparisonCall, stats))
    c.getCode
  }

  //@formatter:off
  /**
    * Transforms an array slice
    *
    * Examples:
    *
    * --------------------------------------------------------------------------------
    *
    *   val a = <arr>[<start>:<end>]
    *
    * becomes:
    *
    *   var $container = <arr>
    *   var $start = < 0|slice.start >            // 0 if slice.start is undefined
    *   var $end = < container.Size()|slice.end > // container.Size() if slice.end is undefined
    *   var $slice = new <arrTpe>[$start - $end]
    *   for(var $i = $start; i < $end; i++)
    *     $slice[$start - $i] = $container[$i]
    *   $slice
    *
    * --------------------------------------------------------------------------------
    */
  //@formatter:on
  private def desugarArraySlice(t: Tree): Tree = {
    if (!t.isInstanceOf[ArraySlice])
      return t

    val arraySlice = t.asInstanceOf[ArraySlice]

    val sliceType = arraySlice.arr.getType
    if (sliceType.isInstanceOf[TObject])
      return arraySlice

    val c = new TreeBuilder

    val arr = arraySlice.arr
    val arrayType = arr.getType
    val arrType = arrayType.asInstanceOf[TArray].tpe
    val container = c.putVarDecl("container", arr)

    val sizeCall = c.createMethodCall(container, "Size", TInt)

    val start = c.putVarDecl("start", arraySlice.start.getOrElse(IntLit(0)))
    val end = c.putVarDecl("end", arraySlice.end.getOrElse(sizeCall).setType(TInt))

    val size = List(Minus(end, start).setType(TInt))
    val typeTree = c.getTypeTree(arrayType.asInstanceOf[TArray].tpe)
    val newArray = Trees.NewArray(typeTree, size).setType(arr)
    val slice = c.putVarDecl("slice", newArray)

    val indexDecl = c.createVarDecl("i", start)
    val indexId = indexDecl.id
    val comparison = LessThan(indexId, end).setType(TBool)
    val post = Assign(indexId, Plus(indexId, IntLit(1)).setType(TInt)).setType(TInt)


    val toSlice = ArrayRead(slice, Minus(indexId, start).setType(TInt)).setType(arraySlice)
    val fromArr = ArrayRead(container, indexId).setType(arrType)
    val copyValue = Assign(toSlice, fromArr).setType(arrType)

    c.put(For(List(indexDecl), comparison, List(post), copyValue))
    c.put(slice)

    c.setPos(arraySlice)

    c.getCode
  }
}


