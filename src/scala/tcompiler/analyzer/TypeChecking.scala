package tcompiler
package analyzer

import tcompiler.analyzer.Symbols._
import tcompiler.analyzer.Types._
import tcompiler.ast.TreeGroups._
import tcompiler.ast.Trees._
import tcompiler.utils.Extensions._
import tcompiler.utils._

import scala.collection.mutable.ArrayBuffer

object TypeChecking extends Pipeline[Program, Program] {

  val LocationPrefix     = "T"
  val hasBeenTypechecked = scala.collection.mutable.Set[MethodSymbol]()
  var methodUsage        = Map[MethodSymbol, Boolean]()


  /**
    * Typechecking does not produce a value, but has the side effect of
    * attaching types to trees and potentially outputting error messages.
    */
  def run(ctx: Context)(prog: Program): Program = {
    // Typecheck fields
    prog.classes.foreach { classDecl =>
      val typeChecker = new TypeChecker(ctx, new MethodSymbol("", classDecl.getSymbol, None, Set()))
      classDecl.vars.foreach(typeChecker.tcStat(_))
    }

    // Typecheck methods
    prog.classes.foreach { classDecl =>
      classDecl.methods.foreach { method =>
        val methodSymbol = method.getSymbol
        if (!methodUsage.contains(methodSymbol))
          methodUsage += methodSymbol -> !method.accessability.isInstanceOf[Private]
        new TypeChecker(ctx, methodSymbol).tcMethod()
      }
    }


    val c = new ClassSymbol("")
    val tc = new TypeChecker(ctx, new MethodSymbol("", c, None, Set()))

    tc.checkMethodUsage()
    tc.checkCorrectOverrideReturnTypes(prog)
    tc.checkTraitsAreImplemented(prog)
    prog
  }


}

class TypeChecker(
  override var ctx: Context,
  currentMethodSymbol: MethodSymbol,
  methodStack: List[MethodSymbol] = List()) extends TypeCheckingErrors {

  import TypeChecking._

  val returnStatements = ArrayBuffer[(Return, Type)]()

  def tcMethod(): Unit = {
    if (TypeChecking.hasBeenTypechecked(currentMethodSymbol))
      return

    if (currentMethodSymbol.getType == TUntyped && methodStack.contains(currentMethodSymbol)) {
      ErrorCantInferTypeRecursiveMethod(currentMethodSymbol)
      return
    }

    currentMethodSymbol.stat.ifDefined(tcStat)

    if (currentMethodSymbol.getType != TUntyped) {
      currentMethodSymbol.setType(tcOperator(currentMethodSymbol.getType))
      return
    }

    if (currentMethodSymbol.getType != TUntyped)
      return

    if (returnStatements.isEmpty) {
      currentMethodSymbol.setType(TUnit)
      return
    }

    if (returnStatements.map(_._2).toSet.size > 1) {
      val s = returnStatements.map { case (stat, tpe) => s"Line ${stat.line} -> '$tpe'" }.mkString(", ")
      ErrorMultipleReturnTypes(s, returnStatements.head._1)
      currentMethodSymbol.setType(TError)
      return
    }

    val inferedType = returnStatements.head._2


    currentMethodSymbol.setType(tcOperator(inferedType))
    TypeChecking.hasBeenTypechecked += currentMethodSymbol
  }

  def tcOperator(tpe: Type) = currentMethodSymbol match {
    case op: OperatorSymbol =>
      // Special rules for some operators
      val correctOperatorType = getCorrectOperatorType(op)
      correctOperatorType match {
        case Some(correctType) if tpe != correctType =>
          ErrorOperatorWrongReturnType(operatorString(op), correctType.toString, tpe.toString, op)
        case _                                       => tpe
      }
    case _                  => tpe
  }

  private def getCorrectOperatorType(op: OperatorSymbol) = {
    op.operatorType match {
      case _: ArrayAssign           => Some(TUnit)
      case _: Hash                  => Some(TInt)
      case ComparisonOperator(_, _) => Some(TBool)
      case EqualsOperator(_, _)     => Some(TBool)
      case _                        => None
    }
  }


  def tcStat(statement: StatTree): Unit = statement match {
    case Block(stats)                              =>
      stats.foreach(tcStat)
    case varDecl@VarDecl(tpe, id, init, modifiers) =>
      tpe match {
        case Some(t) => init match {
          case Some(expr) => tcExpr(expr, t.getType)
          case _          =>
        }
        case None    => init match {
          case Some(expr) =>
            val inferedType = tcExpr(expr)
            id.setType(inferedType)
          case _          => ErrorNoTypeNoInitalizer(varDecl.id.value, varDecl)
        }
      }
    case If(expr, thn, els)                        =>
      tcExpr(expr, TBool)
      tcStat(thn)
      if (els.isDefined)
        tcStat(els.get)
    case While(expr, stat)                         =>
      tcExpr(expr, TBool)
      tcStat(stat)

    case For(init, condition, post, stat) =>
      init.foreach(tcStat(_))
      tcExpr(condition, TBool)
      post.foreach(tcStat)
      tcStat(stat)
    case f: Foreach                       =>
      typeCheckForeachLoop(f)
    case PrintStatement(expr)             =>
      tcExpr(expr) // TODO: Allow unit for println
      if (expr.getType == TUnit)
        ErrorCantPrintUnitType(expr)
    case Error(expr)                      =>
      tcExpr(expr, TString)
    case ret@Return(Some(expr))           =>
      val t = currentMethodSymbol.getType match {
        case TUntyped => tcExpr(expr)
        case retType  => tcExpr(expr, retType)
      }
      returnStatements += ((ret, t))
    case ret@Return(None)                 =>
      if (currentMethodSymbol.getType != TUntyped && currentMethodSymbol.getType != TUnit)
        ErrorWrongReturnType(currentMethodSymbol.getType.toString, ret)
      returnStatements += ((ret, TUnit))
    case _: Break | _: Continue           =>
    case expr: ExprTree                   =>
      tcExpr(expr)
  }

  def tcExpr(expr: ExprTree, expected: Type*): Type = {
    tcExpr(expr, expected.toList)
  }

  def tcExpr(expression: ExprTree, expected: List[Type]): Type = {
    val foundType = expression match {
      case _: IntLit    => TInt
      case _: LongLit   => TLong
      case _: FloatLit  => TFloat
      case _: DoubleLit => TDouble
      case _: CharLit   => TChar
      case _: StringLit => TString
      case _: True      => TBool
      case _: False     => TBool

      case id: Identifier                =>
        id.getSymbol match {
          case varSymbol: VariableSymbol => varSymbol.classSymbol match {
            case Some(clazz) => checkFieldPrivacy(clazz, varSymbol, id)
            case None        =>
          }
          case _                         =>
        }
        id.getType
      case id: ClassIdentifier           => id.getType
      case th: This                      => th.getSymbol.getType
      case su: Super                     => su.getSymbol.getType
      case mc: MethodCall                => tcMethodCall(mc)
      case fr@FieldRead(obj, id)         =>
        val tpe = typeCheckField(obj, id, fr)
        fr.setType(tpe)
        tpe
      case fa@FieldAssign(obj, id, expr) =>
        val tpe = typeCheckField(obj, id, fa)
        fa.setType(tpe)
        tcExpr(expr, tpe)
        checkReassignment(id, fa)
        tpe
      case newArray@NewArray(tpe, sizes) =>
        sizes.foreach(tcExpr(_, TInt))
        var arrayType = tpe.getType
        for (i <- 1 to newArray.dimension)
          arrayType = TArray(arrayType)
        arrayType
      case ArrayLit(expressions)         =>
        val tpes = expressions.map(tcExpr(_))
        if (tpes.isEmpty) {
          TArray(Types.tObject)
        } else {
          val typeSet = tpes.toSet
          if (typeSet.size > 1)
            ErrorMultipleArrayLitTypes(typeSet.map(t => s"'$t'").mkString(", "), expression)
          TArray(tpes.head)
        }
      case Plus(lhs, rhs)                =>
        val args = (tcExpr(lhs), tcExpr(rhs))
        args match {
          case _ if args.anyIs(tObject)       =>
            // Operator overloads have precedence over string addition
            val operatorType = tcBinaryOperatorNoErrors(expression, args)

            operatorType match {
              case TError if args.anyIs(TString) => TString // If other object is a string they can still be added
              case TError                        => ErrorOverloadedOperatorNotFound(expression, List(args._1, args._2), expression)
              case _                             => operatorType
            }
          case _ if args.anyIs(TString)       => TString
          case _ if args.anyIs(TBool, tArray) => ErrorOperatorDoesNotExist(expression, args, expression)
          case _ if args.anyIs(TDouble)       => TDouble
          case _ if args.anyIs(TFloat)        => TFloat
          case _ if args.anyIs(TLong)         => TLong
          case _                              => TInt
        }
      case BinaryOperator(lhs, rhs)      =>
        val args = (tcExpr(lhs), tcExpr(rhs))
        args match {
          case _ if args.anyIs(tObject)                => tcBinaryOperator(expression, args)
          case _ if args.anyIs(TString, TBool, tArray) => ErrorOperatorDoesNotExist(expression, args, expression)
          case _ if args.anyIs(TDouble)                => TDouble
          case _ if args.anyIs(TFloat)                 => TFloat
          case _ if args.anyIs(TLong)                  => TLong
          case _                                       => TInt
        }
      case LogicalOperator(lhs, rhs)     =>
        val args = (tcExpr(lhs), tcExpr(rhs))
        args match {
          case _ if args.anyIs(tObject)                                 => tcBinaryOperator(expression, args)
          case _ if args.bothAre(TBool)                                 => TBool
          case _ if args.anyIs(TBool, TFloat, TDouble, TString, tArray) => ErrorOperatorDoesNotExist(expression, args, expression)
          case _ if args.anyIs(TLong)                                   => TLong
          case _ if args.anyIs(TInt)                                    => TInt
          case _ if args.bothAre(TChar)                                 => TInt
          case _                                                        => ErrorOperatorDoesNotExist(expression, args, expression)
        }
      case ShiftOperator(lhs, rhs)       =>
        val args = (tcExpr(lhs), tcExpr(rhs))
        args match {
          case _ if args.anyIs(tObject)                                 => tcBinaryOperator(expression, args)
          case _ if args.anyIs(TBool, TFloat, TDouble, TString, tArray) => ErrorOperatorDoesNotExist(expression, args, expression)
          case _ if args.anyIs(TLong)                                   => TLong
          case _                                                        => TInt
        }
      case Assign(id, expr)              =>
        id.getType match {
          case objType: TObject => tcExpr(expr, objType)
          case arrType: TArray  => tcExpr(expr, arrType)
          case TString          => tcExpr(expr, TString)
          case TBool            => tcExpr(expr, TBool)
          case TChar            =>
            tcExpr(expr, TInt, TChar)
            TChar
          case TInt             =>
            tcExpr(expr, TInt, TChar)
            TInt
          case TLong            =>
            tcExpr(expr, TLong, TInt, TChar)
            TLong
          case TFloat           =>
            tcExpr(expr, TFloat, TLong, TInt, TChar)
            TFloat
          case TDouble          =>
            tcExpr(expr, TDouble, TFloat, TLong, TInt, TChar)
            TDouble
          case _                => TError
        }
      case ArrayAssign(arr, index, expr) =>
        val arrTpe = tcExpr(arr)
        arrTpe match {
          case TObject(classSymbol) =>
            val (indexType, exprType) = (tcExpr(index), tcExpr(expr))

            val argList = List(indexType, exprType)

            val operatorType = classSymbol.lookupOperator(expression, argList) match {
              case Some(operatorSymbol) =>
                checkOperatorPrivacy(classSymbol, operatorSymbol, expression)
                inferTypeOfMethod(operatorSymbol)
                expression.setType(operatorSymbol.getType)
                operatorSymbol.getType
              case None                 =>
                ErrorIndexingOperatorNotFound(expression, argList, expression, arrTpe.toString)
            }

            if (operatorType != TError && operatorType != TUnit)
              ErrorOperatorWrongReturnType(operatorString(expression, argList, Some(arrTpe.toString)), "Unit", operatorType.toString, expr)
            exprType
          case TArray(arrayTpe)     =>
            tcExpr(index, TInt)
            arrayTpe match {
              case objType: TObject => tcExpr(expr, objType)
              case arrType: TArray  => tcExpr(expr, arrType)
              case TString          => tcExpr(expr, TString)
              case TBool            => tcExpr(expr, TBool)
              case TChar            =>
                tcExpr(expr, TInt, TChar)
                TChar
              case TInt             =>
                tcExpr(expr, TInt, TChar)
                TInt
              case TLong            =>
                tcExpr(expr, TLong, TInt, TChar)
                TLong
              case TFloat           =>
                tcExpr(expr, TFloat, TLong, TInt, TChar)
                TFloat
              case TDouble          =>
                tcExpr(expr, TDouble, TFloat, TLong, TInt, TChar)
                TDouble
              case _                => ???
            }
          case tpe                  => ErrorWrongType(arr.getType + "[]", tpe, arr)
        }
      case ComparisonOperator(lhs, rhs)  =>
        // TODO: String should be allowed
        val args = (tcExpr(lhs), tcExpr(rhs))
        args match {
          case _ if args.anyIs(tObject)                => tcBinaryOperator(expression, args, Some(TBool))
          case _ if args.anyIs(TBool, TString, tArray) => ErrorOperatorDoesNotExist(expression, args, expression)
          case _                                       => TBool
        }
      case EqualsOperator(lhs, rhs)      =>
        val args@(lhsType, rhsType) = (tcExpr(lhs), tcExpr(rhs))
        args match {
          case _ if args.anyIs(tObject)                       =>
            val argList = List(lhsType, rhsType)
            val operatorType = tcBinaryOperatorNoErrors(expression, args)
            operatorType match {
              case TError if args.bothAre(tObject) => // If both are objects they can be compared by reference
              case TError                          => ErrorOverloadedOperatorNotFound(expression, argList, expression)
              case _                               => correctOperatorType(expression, argList, Some(TBool), operatorType)
            }
          case (x, y) if x.isSubTypeOf(y) || y.isSubTypeOf(x) => // Valid
          case _ if args.anyIs(TBool, TString, tArray)        => ErrorOperatorDoesNotExist(expression, args, expression)
          case _                                              => // Valid
        }
        TBool
      case And(lhs, rhs)                 =>
        tcExpr(lhs, TBool)
        tcExpr(rhs, TBool)
        TBool
      case Or(lhs, rhs)                  =>
        tcExpr(lhs, TBool)
        tcExpr(rhs, TBool)
        TBool
      case Not(expr)                     =>
        tcExpr(expr, TBool, Types.tObject) match {
          case x: TObject => tcUnaryOperator(expression, x, Some(TBool))
          case _          =>
        }
        TBool
      case Instance(expr, id)            =>
        val tpe = tcExpr(expr)
        tpe match {
          case t: TObject =>
            tcExpr(id, tpe.getSuperTypes)
            TBool
          case _          => ErrorWrongType("object", tpe, expr)
        }
      case As(expr, tpe)                 =>
        tcExpr(expr, tpe.getType.getSuperTypes)
        tpe.getType
      case ArrayRead(arr, index)         =>
        val arrTpe = tcExpr(arr)
        arrTpe match {
          case TObject(classSymbol) =>
            val indexType = tcExpr(index)
            val argList = List(indexType)

            classSymbol.lookupOperator(expression, argList) match {
              case Some(operatorSymbol) =>
                checkOperatorPrivacy(classSymbol, operatorSymbol, expression)
                inferTypeOfMethod(operatorSymbol)
                expression.setType(operatorSymbol.getType)
                operatorSymbol.getType
              case None                 =>
                ErrorIndexingOperatorNotFound(expression, argList, expression, arrTpe.toString)
            }
          case TArray(arrTpe)       =>
            tcExpr(index, TInt)
            arrTpe
          case tpe                  => ErrorWrongType("array", tpe, arr)
        }
      case ArraySlice(arr, start, end)   =>
        start.ifDefined(tcExpr(_, TInt))
        end.ifDefined(tcExpr(_, TInt))
        tcExpr(arr)
      case newDecl@New(tpe, exprs)       =>
        val argTypes = exprs.map(tcExpr(_))
        tpe.getType match {
          case TObject(classSymbol) =>
            classSymbol match {
              case _: TraitSymbol => ErrorInstantiateTrait(classSymbol.name, newDecl)
              case _              =>
                classSymbol.lookupMethod("new", argTypes) match {
                  case Some(constructorSymbol) => checkConstructorPrivacy(classSymbol, constructorSymbol, newDecl)
                  case None if exprs.nonEmpty  =>
                    val methodSignature = tpe.name + exprs.map(_.getType).mkString("(", " , ", ")")
                    ErrorDoesntHaveConstructor(classSymbol.name, methodSignature, newDecl)
                  case _                       =>
                }
            }
          case primitiveType        =>
            if (exprs.size > 1) {
              ErrorNewPrimitive(tpe.name, argTypes, newDecl)
            } else if (exprs.size == 1) {
              val arg = exprs.head.getType
              if (!primitiveType.isImplicitlyConvertableFrom(arg))
                ErrorNewPrimitive(tpe.name, argTypes, newDecl)
            }
        }
        tpe.getType
      case Negation(expr)                =>
        tcExpr(expr, Types.tObject :: Types.primitives) match {
          case x: TObject => tcUnaryOperator(expression, x)
          case TChar      => TInt // Negation of char is int
          case x          => x
        }
      case Hash(expr)                    =>
        val exprType = tcExpr(expr, TString :: Types.tObject :: Types.primitives)
        exprType match {
          case _: TObject =>
            val argList = List(exprType)
            val operatorType = typeCheckOperator(exprType, expression, argList) match {
              case Some(tpe) => tpe match {
                case TInt => tpe
                case _    => ErrorWrongType(TInt, tpe, expression)
              }
              case _         => TInt
            }
            operatorType match {
              case TInt => TInt
              case _    =>
                ErrorOperatorWrongReturnType(operatorString(expression, argList, Some(exprType.toString)), "Int", operatorType.toString, expression)
            }
          case _          => TInt
        }
      case IncrementDecrement(obj)       =>
        obj match {
          case id: Identifier              => checkReassignment(id, expression)
          case _: ArrayRead | _: FieldRead =>
          case _                           => ErrorInvalidIncrementDecrementExpr(expression, obj)
        }
        tcExpr(obj, Types.tObject :: Types.primitives) match {
          case x: TObject => tcUnaryOperator(expression, x, Some(x)) // Requires same return type as type
          case x          => x
        }
      case LogicNot(expr)                =>
        tcExpr(expr, Types.tObject, TInt, TLong, TChar) match {
          case x: TObject => tcUnaryOperator(expression, x)
          case TLong      => TLong
          case _          => TInt
        }
      case Ternary(condition, thn, els)  =>
        tcExpr(condition, TBool)
        val thenType = tcExpr(thn)
        tcExpr(els, thenType)
    }

    // Check result and return a valid type in case of error
    val res =
      if (expected.nonEmpty &&
        (!expected.exists(e => foundType.isSubTypeOf(e) || e.isImplicitlyConvertableFrom(foundType)))) {
        ErrorWrongType(expected, foundType, expression)
      } else {
        foundType
      }
    expression.setType(res)
    res
  }

  def tcMethodCall(mc: MethodCall): Type = {
    val obj = mc.obj
    val meth = mc.meth
    val methodCallArgs = mc.args

    var objType = tcExpr(obj)
    val argTypes = methodCallArgs.map(tcExpr(_))

    def methSignature = meth.value + methodCallArgs.map(_.getType).mkString("(", ", ", ")")


    // Set type of super call based on the method called
    // if it needs to be looked up dynamically
    obj match {
      case s@Super(None) if s.getSymbol.parents.nonEmpty =>
        // supers symbol is set to this in name analyzer so we can look up the
        // desired method
        val thisSymbol = s.getSymbol
        thisSymbol.lookupParentMethod(meth.value, argTypes) match {
          case Some(methodSymbol) =>
            val classSymbol = methodSymbol.classSymbol
            objType = classSymbol.getType
            s.setSymbol(classSymbol)
            s.setType(objType)
          case None               =>
            return ErrorNoSuperTypeHasMethod(thisSymbol.name, methSignature, mc)
        }
      case _                                             =>
    }

    val tpe = objType match {
      case TObject(classSymbol) =>
        classSymbol.lookupMethod(meth.value, argTypes) match {
          case Some(methSymbol) =>
            checkMethodPrivacy(classSymbol, methSymbol, mc)
            checkStaticMethodConstraints(obj, classSymbol, methSymbol, mc)

            TypeChecking.methodUsage += methSymbol -> true
            inferTypeOfMethod(methSymbol)
            meth.setSymbol(methSymbol)
            meth.getType
          case None             =>
            ErrorClassDoesntHaveMethod(classSymbol.name, methSignature, mc)
        }
      case TArray(arrTpe)       =>
        if (methodCallArgs.nonEmpty || meth.value != "Size")
          ErrorMethodOnWrongType(methSignature, objType.toString, mc)
        TInt
      case _                    =>
        ErrorMethodOnWrongType(methSignature, objType.toString, mc)
    }
    mc.setType(tpe)
    tpe
  }


  def tcBinaryOperatorNoErrors(expr: ExprTree, args: (Type, Type)): Type = {
    if (args._1 == TError || args._2 == TError)
      return TError

    val argList = List(args._1, args._2)
    typeCheckOperator(args._1, expr, argList) match {
      case Some(tpe)                  => tpe
      case None if args._1 != args._2 =>
        typeCheckOperator(args._2, expr, argList) match {
          case Some(tpe) => tpe
          case None      => TError
        }
      case _                          => TError
    }
  }

  def tcBinaryOperator(expr: ExprTree, args: (Type, Type), expectedType: Option[Type] = None): Type = {
    val operatorType = tcBinaryOperatorNoErrors(expr, args)
    val argList = List(args._1, args._2)
    if (operatorType == TError)
      ErrorOverloadedOperatorNotFound(expr, argList, expr)
    else
      correctOperatorType(expr, argList, expectedType, operatorType)
  }

  def tcUnaryOperator(expr: ExprTree, arg: Type, expectedType: Option[Type] = None): Type = {
    val argList = List(arg)
    val operatorType = typeCheckOperator(arg, expr, argList) match {
      case Some(tpe) => tpe
      case None      => ErrorOverloadedOperatorNotFound(expr, argList, expr)
    }
    correctOperatorType(expr, argList, expectedType, operatorType)
  }

  def checkMethodUsage() = {
    // Check method usage
    // TODO: Refactoring of typechecker global variables etc.
    methodUsage foreach {
      case (method, used) =>
        if (!used)
          WarningUnusedPrivateField(method.signature, method)
    }
    methodUsage = Map[MethodSymbol, Boolean]()
  }

  def checkCorrectOverrideReturnTypes(prog: Program) =
    prog.classes.foreach { clazz =>
      clazz.methods.foreach { meth =>
        val classSymbol = clazz.getSymbol
        val methSymbol = meth.getSymbol

        classSymbol.lookupParentMethod(methSymbol.name, methSymbol.argTypes) match {
          case Some(parentMeth) if parentMeth.getType != meth.getSymbol.getType =>
            val parentType = parentMeth.getType
            val tpe = meth.getSymbol.getType
            if (!tpe.isSubTypeOf(parentType)) {
              ErrorOverridingMethodDifferentReturnType(meth.getSymbol.signature,
                classSymbol.name,
                meth.getSymbol.getType.toString,
                parentMeth.classSymbol.name,
                parentMeth.getType.toString,
                meth)
            }
          case _                                                                =>
        }
      }
    }


  def checkTraitsAreImplemented(prog: Program) =
    prog.classes.filter(!_.isInstanceOf[Trait]).foreach { classDecl =>
      classDecl.getImplementedTraits.foreach(t => traitIsImplemented(classDecl, t.getSymbol))
    }

  private def checkReassignment(id: Identifier, pos: Positioned) = {
    if (id.hasSymbol) {
      val varSymbol = id.getSymbol.asInstanceOf[VariableSymbol]
      if (varSymbol.modifiers.contains(Final()))
        ErrorReassignmentToVal(id.value, pos)
    }
  }

  private def traitIsImplemented(classDecl: ClassDecl, implementedTrait: ClassSymbol) = {
    val unimplementedMethods = implementedTrait.unimplementedMethods()
    unimplementedMethods.foreach { case (method, owningTrait) =>
      if (!classDecl.getSymbol.implementsMethod(method))
        ErrorUnimplementedMethodFromTrait(classDecl.id.value,
          method.signature,
          owningTrait.name, classDecl.id)
    }
  }

  private def typeCheckForeachLoop(forEach: Foreach): Unit = {
    val varDecl = forEach.varDecl
    val container = forEach.container
    val containerType = tcExpr(container)
    val expectedVarType = containerType match {
      case TArray(arrTpe)       =>
        arrTpe
      case TObject(classSymbol) =>
        getIteratorType(classSymbol) match {
          case Some(t) => t
          case None    =>
            ErrorForeachContainNotIterable(containerType, container)
            return
        }
      case _                    =>
        ErrorForeachContainNotIterable(containerType, container)
        return
    }
    varDecl.id.getType match {
      case TUntyped => varDecl.id.setType(expectedVarType)
      case tpe      =>
        if (tpe != expectedVarType)
          ErrorWrongType(expectedVarType, tpe, varDecl.id)
    }
    tcStat(forEach.stat)
  }

  private def getIteratorType(classSymbol: ClassSymbol): Option[Type] = {
    classSymbol.lookupMethod("Iterator", List()) match {
      case Some(methSymbol) =>
        inferTypeOfMethod(methSymbol)
        methSymbol.getType match {
          case TObject(methodClassSymbol) =>
            methodClassSymbol.lookupMethod("HasNext", List()) match {
              case Some(hasNextMethod) =>
                inferTypeOfMethod(hasNextMethod)
                if (hasNextMethod.getType != TBool)
                  return None
              case None                => return None
            }
            methodClassSymbol.lookupMethod("Next", List()) match {
              case Some(nextMethod) =>
                inferTypeOfMethod(nextMethod)
                return Some(nextMethod.getType)
              case None             =>
            }
          case _                          =>
        }
      case None             =>
    }
    None
  }

  private def inferTypeOfMethod(methodSymbol: MethodSymbol) = {
    if (methodSymbol.getType == TUntyped)
      new TypeChecker(ctx, methodSymbol, currentMethodSymbol :: methodStack).tcMethod()
  }

  private def typeCheckOperator(classType: Type, operator: ExprTree, args: List[Type]): Option[Type] =
    classType match {
      case TObject(classSymbol) =>
        classSymbol.lookupOperator(operator, args) match {
          case Some(operatorSymbol) =>
            checkOperatorPrivacy(classSymbol, operatorSymbol, operator)
            inferTypeOfMethod(operatorSymbol)
            operator.setType(operatorSymbol.getType)
            Some(operatorSymbol.getType)
          case None                 => None
        }
      case _                    => None
    }

  private def correctOperatorType(expr: ExprTree, args: List[Type], expectedType: Option[Type], found: Type): Type = {

    // No need to report another error if one has already been found
    if (found == TError)
      return TError

    expectedType match {
      case Some(expected) =>
        if (found != expected)
          ErrorOperatorWrongReturnType(operatorString(expr, args), expected.toString, found.toString, expr)
        else found
      case _              => found
    }
  }


  private def typeCheckField(obj: ExprTree, fieldId: Identifier, pos: Positioned): Type = {
    var objType = tcExpr(obj)
    val fieldName = fieldId.value

    // Set type of super call based on the method called
    // if it needs to be looked up dynamically
    obj match {
      case s@Super(None) if s.getSymbol.parents.nonEmpty =>
        // supers symbol is set to this in name analyzer so we can look up the
        // desired method
        val thisSymbol = s.getSymbol
        thisSymbol.lookupParentVar(fieldName) match {
          case Some(variableSymbol) =>
            val classSymbol = variableSymbol.classSymbol.get
            objType = classSymbol.getType
            s.setSymbol(classSymbol)
            s.setType(objType)
          case None                 => return ErrorNoSuperTypeHasField(thisSymbol.name, fieldName, pos)
        }
      case _                                             =>
    }

    objType match {
      case TObject(classSymbol) =>
        classSymbol.lookupVar(fieldId.value) match {
          case Some(varSymbol) =>
            checkFieldPrivacy(classSymbol, varSymbol, pos)
            checkStaticFieldConstraints(obj, classSymbol, varSymbol, pos)
            fieldId.setSymbol(varSymbol)
            fieldId.getType
          case None            =>
            ErrorClassDoesntHaveField(classSymbol.name, fieldId.value, pos)
        }
      case _                    => ErrorFieldOnWrongType(objType.toString, pos)
    }
  }

  private def checkStaticMethodConstraints(obj: ExprTree, classSymbol: ClassSymbol, methodSymbol: MethodSymbol, pos: Positioned) = {
    if (!methodSymbol.isStatic && isStaticCall(obj))
      ErrorNonStaticMethodAsStatic(methodSymbol.name, pos)

    if (obj.isInstanceOf[This] && currentMethodSymbol.isStatic && !methodSymbol.isStatic)
      ErrorNonStaticMethodFromStatic(methodSymbol.name, pos)
  }

  private def checkStaticFieldConstraints(obj: ExprTree, classSymbol: ClassSymbol, varSymbol: VariableSymbol, pos: Positioned) = {
    if (!varSymbol.isStatic && isStaticCall(obj))
      ErrorNonStaticFieldAsStatic(varSymbol.name, pos)

    if (obj.isInstanceOf[This] && currentMethodSymbol.isStatic && !varSymbol.isStatic)
      ErrorNonStaticFieldFromStatic(varSymbol.name, pos)
  }

  private def checkConstructorPrivacy(classSymbol: ClassSymbol, methodSymbol: MethodSymbol, pos: Positioned): Unit =
    if (!checkPrivacy(classSymbol, methodSymbol.accessability))
      ErrorConstructorPrivacy(methodSymbol, classSymbol.name, currentMethodSymbol.classSymbol.name, pos)

  private def checkMethodPrivacy(classSymbol: ClassSymbol, methodSymbol: MethodSymbol, pos: Positioned): Unit =
    if (!checkPrivacy(classSymbol, methodSymbol.accessability))
      ErrorMethodPrivacy(methodSymbol, classSymbol.name, currentMethodSymbol.classSymbol.name, pos)

  private def checkFieldPrivacy(classSymbol: ClassSymbol, varSymbol: VariableSymbol, pos: Positioned): Unit =
    if (!checkPrivacy(classSymbol, varSymbol.accessability))
      ErrorFieldPrivacy(varSymbol, classSymbol.name, currentMethodSymbol.classSymbol.name, pos)

  private def checkOperatorPrivacy(classSymbol: ClassSymbol, opSymbol: OperatorSymbol, pos: Positioned): Unit =
    if (!checkPrivacy(classSymbol, opSymbol.accessability))
      ErrorOperatorPrivacy(opSymbol, classSymbol.name, currentMethodSymbol.classSymbol.name, pos)

  private def checkPrivacy(classSymbol: ClassSymbol, access: Accessability) = access match {
    case Public()                                                                                => true
    case Private() if classSymbol == currentMethodSymbol.classSymbol                             => true
    case Protected() if currentMethodSymbol.classSymbol.getType.isSubTypeOf(classSymbol.getType) => true
    case _                                                                                       => false
  }

}
