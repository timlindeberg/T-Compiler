package tcompiler
package analyzer

import tcompiler.analyzer.Symbols._
import tcompiler.analyzer.Types._
import tcompiler.ast.TreeGroups.{IncrementDecrement, UnaryOperatorDecl, BinaryOperatorDecl}
import tcompiler.ast.Trees.{ClassDecl, _}
import tcompiler.ast.{Printer, Trees}
import tcompiler.utils._

object NameAnalysis extends Pipeline[Program, Program] {

  def run(ctx: Context)(prog: Program): Program = {
    val nameAnalyzer = new NameAnalyser(ctx, prog, new GlobalScope)
    nameAnalyzer.addSymbols()
    nameAnalyzer.bindIdentifiers()
    nameAnalyzer.checkInheritanceCycles()
    nameAnalyzer.checkVariableUsage()
    nameAnalyzer.checkOverrideAndOverLoadConstraints()
    prog
  }

  class NameAnalyser(ctx: Context, prog: Program, globalScope: GlobalScope) {

    import ctx.reporter._

    var variableUsage: Map[VariableSymbol, Boolean] = Map()

    def addSymbols() = SymbolAdder()

    def bindIdentifiers() = SymbolBinder()

    def checkInheritanceCycles(): Unit = {

      def inheritanceList(set: Set[ClassSymbol], c: ClassSymbol): String =
        (if (set.size >= 2) set.map(_.name).mkString(" <: ")
        else c.name) + " <: " + c.name

      globalScope.classes.foreach { x =>
        var classSymbol: Option[ClassSymbol] = Some(x._2)
        var set: Set[ClassSymbol] = Set()
        while (classSymbol.isDefined) {
          val c = classSymbol.get
          if (set.contains(c)) {
            error("A cycle was found in the inheritence graph: " + inheritanceList(set, c), c)
            return
          }
          set += c
          classSymbol = c.parent
        }
      }
    }

    def error(msg: String, tree: Positioned) = {
      tree match {
        case id: Identifier => id.setSymbol(new ErrorSymbol)
        case _              =>
      }

      ctx.reporter.error(msg, tree)
    }

    def methodsEquals(meth1: MethodSymbol, meth2: MethodSymbol): Boolean = {
      val (list1, list2) = (meth1.argList, meth2.argList)
      val sameSize = list1.size == list2.size
      val sameArgs = list1.zip(list2).forall { case (x, y) => x.getType == y.getType }
      val sameRetType = meth1.getType == meth2.getType

      sameSize && sameArgs && sameRetType
    }

    def checkOverrideAndOverLoadConstraints() = {
      prog.classes.foreach { klass =>
        klass.methods.foreach {
          case meth: MethodDecl             =>
            val methSymbol = meth.getSymbol
            val methName = meth.id.value
            val argTypes = meth.args.map(_.tpe.getType)
            methSymbol.classSymbol.parent match {
              case Some(parent) =>
                parent.lookupMethod(methName, argTypes) match {
                  case Some(parentMethSymbol) =>
                    methSymbol.overridden = Some(parentMethSymbol)
                  case None                   =>
                }
              case None         =>
            }
          case constructor: ConstructorDecl => // TODO
          case operator: OperatorDecl       =>
            val operatorSymbol = operator.getSymbol
            val argTypes = operator.args.map(_.tpe.getType)
            operatorSymbol.classSymbol.parent match {
              case Some(parent) =>
                parent.lookupOperator(operator.operatorType, argTypes) match {
                  case Some(parentMethSymbol) =>
                    error("Operator overloads cannot be overriden.", operator)
                  case None                   =>
                }
              case None         =>
            }
        }
      }
    }

    def checkVariableUsage() = {
      variableUsage foreach {
        case (variable, used) =>
          if (!used) warning("Variable \'" + variable.name + "\' declared but is never used:", variable)
      }
    }

    object SymbolAdder {
      def apply(): Unit = addSymbols(prog, globalScope)

      private def addSymbols(t: Tree, globalScope: GlobalScope): Unit = t

      match {
        case Program(_, _, main, classes)                                                    =>
          if (main.isDefined)
            addSymbols(main.get, globalScope)
          classes.foreach(addSymbols(_, globalScope))
        case mainObject @ MainObject(id, stats)                                              =>
          globalScope.mainClass = new ClassSymbol(id.value).setPos(id)
          mainObject.setSymbol(globalScope.mainClass)
          id.setSymbol(globalScope.mainClass)
        case classDecl @ ClassDecl(id @ ClassIdentifier(name, types), parent, vars, methods) =>
          val newSymbol = new ClassSymbol(name).setPos(id)
          ensureIdentiferNotDefined(globalScope.classes, id.value, id)
          id.setSymbol(newSymbol)
          classDecl.setSymbol(newSymbol)
          globalScope.classes += (id.value -> newSymbol)
          vars.foreach(addSymbols(_, newSymbol))
          methods.foreach(addSymbols(_, newSymbol))

          if (name == globalScope.mainClass.name)
            error("Class \'" + name + "\' has the same name as the main object.", id)
        case _                                                                               => throw new UnsupportedOperationException
      }

      private def addSymbols(t: Tree, s: ClassSymbol): Unit = t match {
        case varDecl @ VarDecl(tpe, id, init, access)                                          =>
          val newSymbol = new VariableSymbol(id.value, access).setPos(id)
          ensureIdentiferNotDefined(s.members, id.value, id)
          id.setSymbol(newSymbol)
          varDecl.setSymbol(newSymbol)
          variableUsage += newSymbol -> true
          s.members += (id.value -> newSymbol)
        case methodDecl @ MethodDecl(retType, id, args, vars, stats, access)                   =>
          val newSymbol = new MethodSymbol(id.value, s, access).setPos(id)
          id.setSymbol(newSymbol)
          methodDecl.setSymbol(newSymbol)

          args.foreach(addSymbols(_, newSymbol))
          vars.foreach(addSymbols(_, newSymbol))
        case constructorDecl @ ConstructorDecl(id, args, vars, stats, access)                  =>
          val newSymbol = new MethodSymbol(id.value, s, access).setPos(id)
          newSymbol.setType(TUnit)

          id.setSymbol(newSymbol)
          constructorDecl.setSymbol(newSymbol)

          args.foreach(addSymbols(_, newSymbol))
          vars.foreach(addSymbols(_, newSymbol))
        case operatorDecl @ OperatorDecl(operatorType, retType, args, vars, stats, access, id) =>
          val newSymbol = new OperatorSymbol(operatorType, s, access)
          id.setSymbol(newSymbol)
          operatorDecl.setSymbol(newSymbol)

          args.foreach(addSymbols(_, newSymbol))
          vars.foreach(addSymbols(_, newSymbol))
        case _                                                                                 => throw new UnsupportedOperationException
      }

      private def addSymbols(t: Tree, s: MethodSymbol): Unit = t match {
        case varDecl @ VarDecl(tpe, id, init, _) =>
          val newSymbol = new VariableSymbol(id.value).setPos(id)
          ensureIdentiferNotDefined(s.members, id.value, id)
          if (s.params.contains(id.value)) {
            val oldSymbol = s.params(id.value)
            error("Local variable \'" + id.value + "\' shadows method parameter defined at " + oldSymbol.line + ":" + oldSymbol.col, id)
          }
          id.setSymbol(newSymbol)
          varDecl.setSymbol(newSymbol)
          variableUsage += newSymbol -> true
          s.members += (id.value -> newSymbol)
        case formal @ Formal(tpe, id)            =>
          val newSymbol = new VariableSymbol(id.value).setPos(id)
          ensureIdentiferNotDefined(s.params, id.value, id)
          id.setSymbol(newSymbol)
          formal.setSymbol(newSymbol)
          s.params += (id.value -> newSymbol)
          s.argList ++= List(newSymbol)
        case _                                   => throw new UnsupportedOperationException
      }

      private def ensureIdentiferNotDefined[T <: Symbol](map: Map[String, T], id: String, pos: Positioned): Unit = {
        if (map.contains(id)) {
          val oldSymbol = map(id)
          error("Variable \'" + id + "\' is already defined at " + oldSymbol.line + ":" + oldSymbol.col, pos)
        }
      }

    }

    object SymbolBinder {
      def apply(): Unit = bind(prog)

      private def bind(list: List[Tree]): Unit = list.foreach(bind)

      private def bind(t: Tree): Unit = t match {
        case Program(_, _, main, classes)                                                =>
          if (main.isDefined)
            bind(main.get)
          bind(classes)
        case main @ MainObject(id, stats)                                                => stats.foreach(bind(_, main.getSymbol))
        case classDecl @ ClassDecl(id, parent, vars, methods)                            =>
          setParent(id, parent, classDecl)
          val sym = classDecl.getSymbol
          sym.setType(TObject(sym))
          val p = classDecl.getSymbol.parent
          if (p.isDefined) {
            vars.foreach(variable => {
              val v = p.get.lookupVar(variable.id.value)
              if (v.isDefined)
                error("Field \'" + variable.getSymbol.name + "\' already defined in super class: ", variable)
            })
          }
          vars.foreach { case VarDecl(tpe, id, init, _) =>
            setType(tpe, id)
            init match {
              case Some(expr) => bind(expr, classDecl.getSymbol)
              case None       =>
            }
          }
          bind(methods)
        case methDecl @ MethodDecl(retType, _, args, vars, stats, _)                     =>
          setType(retType)

          methDecl.getSymbol.setType(retType.getType)

          bind(args)
          bindVars(vars, methDecl.getSymbol)

          ensureMethodNotDefined(methDecl)

          stats.foreach(bind(_, methDecl.getSymbol))
        case constructorDecl @ ConstructorDecl(_, args, vars, stats, _)                  =>
          bind(args)
          bindVars(vars, constructorDecl.getSymbol)

          ensureMethodNotDefined(constructorDecl)
          stats.foreach(bind(_, constructorDecl.getSymbol))
        case operatorDecl @ OperatorDecl(operatorType, retType, args, vars, stats, _, _) =>
          setType(retType)

          operatorDecl.getSymbol.setType(retType.getType)
          operatorType.setType(retType.getType)
          operatorType match {
            case IncrementDecrement(id) => id.setSymbol(new VariableSymbol("")).setType(retType.getType)
            case _                      =>
          }
          bind(args)
          bindVars(vars, operatorDecl.getSymbol)

          ensureOperatorNotDefined(operatorDecl)

          stats.foreach(bind(_, operatorDecl.getSymbol))
        case Formal(tpe, id)                                                             => setType(tpe, id)
        case _                                                                           => throw new UnsupportedOperationException
      }

      private def bindVars(vars: List[VarDecl], symbol: MethodSymbol): Unit =
        vars.foreach { case VarDecl(tpe, id, init, _) =>
          setType(tpe, id)
          init match {
            case Some(expr) => bind(expr, symbol)
            case None       =>
          }
        }

      private def bind(t: Tree, s: Symbol): Unit =
        Trees.traverse(t, (parent, current) => Some(current) collect {
          case MethodCall(obj, meth, args) =>
            bind(obj, s)
            args.foreach(bind(_, s))
          case Instance(expr, id)          =>
            bind(expr, s)
            globalScope.lookupClass(id.value) match {
              case Some(classSymbol) =>
                id.setSymbol(classSymbol)
                id.setType(TObject(classSymbol))
              case None              => error("Type \'" + id.value + "\' was not declared:", id)
            }
          case FieldRead(obj, id)          =>
            bind(obj, s)
          case FieldAssign(obj, id, expr)  =>
            bind(obj, s)
            bind(expr, s)
          case id: Identifier              => parent match {
            case _: MethodCall  =>
            case _: Instance    =>
            case _: FieldRead   =>
            case _: FieldAssign =>
            case _              => setVariable(id, s)
          }
          case id: ClassIdentifier         => setType(id)
          case NewArray(tpe, size)         => setType(tpe)
          case thisSym: This               =>
            s match {
              case classSymbol: ClassSymbol   => thisSym.setSymbol(globalScope.mainClass)
              case methodSymbol: MethodSymbol => thisSym.setSymbol(methodSymbol.classSymbol)
              case _                          => throw new UnsupportedOperationException
            }
        })

      private def ensureMethodNotDefined(meth: FuncTree): Unit = {
        val name = meth.id.value
        val argTypes = meth.args.map(_.tpe.getType)
        meth.getSymbol.classSymbol.lookupMethod(name, argTypes, recursive = false) match {
          case Some(oldMeth) =>
            error("Method \'" + meth.signature + "\' is already defined at line " + oldMeth.line + ".", meth)
          case None          =>
            meth.getSymbol.classSymbol.addMethod(meth.getSymbol)
        }
      }

      private def ensureOperatorNotDefined(operator: OperatorDecl): Unit = {
        val operatorType = operator.operatorType
        val argTypes = operator.args.map(_.tpe.getType)
        def errorString(expected: Int) = "Operator \'" + Printer(operatorType) + "\' has wrong number of arguments: \'" +
          argTypes.mkString(", ") + "\'. Expected " + expected + " argument(s), found" + argTypes.size + "."

        operatorType match {
          case UnaryOperatorDecl(expr) if argTypes.size != 1      =>
            error(errorString(1), operator)
          case BinaryOperatorDecl(lhs, rhs) if argTypes.size != 2 =>
            error(errorString(2), operator)
          case _                                                  =>
        }

        operator.getSymbol.classSymbol.lookupOperator(operatorType, argTypes, recursive = true) match {
          case Some(oldOperator) =>
            error("Operator \'" + Trees.operatorString(operatorType, argTypes) + "\' is already defined at line " + oldOperator.line + ".", operator)
          case None              =>
            operator.getSymbol.classSymbol.addOperator(operator.getSymbol.asInstanceOf[OperatorSymbol])
        }
      }

      private def setType(tpe: TypeTree, id: Identifier): Unit = {
        def set(t: Type): Unit = {
          id.setType(t)
          tpe.setType(t)
        }
        tpe match {
          case tpeId @ ClassIdentifier(typeName, _) =>
            globalScope.lookupClass(typeName) match {
              case Some(classSymbol) =>
                tpeId.setSymbol(classSymbol)
                set(TObject(classSymbol))
              case None              =>
                tpeId.setSymbol(new ErrorSymbol)
                error("Type \'" + tpeId.value + "\' was not declared:", tpeId)
            }
          case BooleanType()                        => set(TBool)
          case IntType()                            => set(TInt)
          case LongType()                           => set(TLong)
          case FloatType()                          => set(TFloat)
          case DoubleType()                         => set(TDouble)
          case CharType()                           => set(TChar)
          case ArrayType(arrayTpe)                  =>
            setType(arrayTpe)
            set(TArray(arrayTpe.getType))
          case StringType()                         => set(TString)
          case UnitType()                           => set(TUnit)
        }
      }

      private def setType(tpe: TypeTree): Unit = {
        tpe match {
          case tpeId @ ClassIdentifier(typeName, _) =>
            globalScope.lookupClass(typeName) match {
              case Some(classSymbol) =>
                tpeId.setSymbol(classSymbol)
                tpeId.setType(TObject(classSymbol))
              case None              =>
                tpeId.setSymbol(new ErrorSymbol)
                error("Type \'" + tpeId.value + "\' was not declared:", tpeId)
            }
          case BooleanType()                        => tpe.setType(TBool)
          case IntType()                            => tpe.setType(TInt)
          case LongType()                           => tpe.setType(TLong)
          case FloatType()                          => tpe.setType(TFloat)
          case DoubleType()                         => tpe.setType(TDouble)
          case CharType()                           => tpe.setType(TChar)
          case ArrayType(arrayTpe)                  =>
            setType(arrayTpe)
            tpe.setType(TArray(arrayTpe.getType))
          case StringType()                         => tpe.setType(TString)
          case UnitType()                           => tpe.setType(TUnit)
        }
      }

      private def setVariable(id: Identifier, s: Symbol): Unit = {
        def errorMsg(name: String) = "Variable \'" + name + "\' was not declared: "
        s match {
          case methodSymbol: MethodSymbol =>
            methodSymbol.lookupVar(id.value) match {
              case Some(symbol) =>
                id.setSymbol(symbol)
                variableUsage += symbol.asInstanceOf[VariableSymbol] -> true
              case None         => error(errorMsg(id.value), id)
            }
          case classSymbol: ClassSymbol   =>
            classSymbol.lookupVar(id.value) match {
              case Some(symbol) =>
                id.setSymbol(symbol)
                variableUsage += symbol.asInstanceOf[VariableSymbol] -> true
              case None         => error(errorMsg(id.value), id)
            }
        }
      }

      private def setParent(id: ClassIdentifier, parent: Option[ClassIdentifier], classDecl: ClassDecl): Unit = {
        if (parent.isDefined) {
          val p = parent.get
          globalScope.lookupClass(p.value) match {
            case Some(parentSymbol) => {
              p.setSymbol(parentSymbol)
              classDecl.getSymbol.parent = Some(parentSymbol)
            }
            case None               => error("Parent class \'" + p.value + "\' is not declared: ", p)
          }
        }
      }
    }

  }

}