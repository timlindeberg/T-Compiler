package tlang
package compiler
package lowering

import tlang.compiler.analyzer.Symbols.{ClassSymbol, MethodSymbol, VariableSymbol}
import tlang.compiler.analyzer.Types._
import tlang.compiler.ast.Trees._
import tlang.compiler.imports.Imports
import tlang.utils.Positioned

import scala.collection.mutable.ListBuffer

object TreeBuilder {
  val ThisName = "$this"
}

case class TreeBuilder() {
  val code: ListBuffer[StatTree] = ListBuffer()

  def put(stat: StatTree): code.type = {
    stat foreach {
      case t: Typed if !t.hasType => sys.error(s"Tree $t does not have a type!")
      case _                      =>
    }
    code += stat
  }

  def putVarDecl(idName: String, initExpression: ExprTree): VariableID = {
    val decl = createVarDecl(idName, initExpression)
    code += decl
    decl.id
  }

  def createMethodCall(obj: ExprTree, classSymbol: ClassSymbol, methName: String, imports: Imports, args: List[ExprTree]): NormalAccess = {
    val methodSymbol = classSymbol.lookupMethod(methName, args.map(_.getType), imports).get
    createMethodCall(obj, methodSymbol, args)
  }

  def createMethodCall(obj: ExprTree, name: String, tpe: Type): NormalAccess =
    createMethodCall(obj, createMethodSymbol(name, tpe))

  def createMethodCall(obj: ExprTree, methodSymbol: MethodSymbol, args: ExprTree*): NormalAccess =
    createMethodCall(obj, methodSymbol, args.toList)

  def createMethodCall(obj: ExprTree, methodSymbol: MethodSymbol, args: List[ExprTree]): NormalAccess = {
    val tpe = methodSymbol.getType
    val sizeMethId = createMethodId(methodSymbol)
    val methCall = MethodCall(sizeMethId, args).setType(tpe)
    NormalAccess(obj, methCall).setType(tpe)
  }

  private def createMethodSymbol(name: String, tpe: Type) =
    new MethodSymbol(name, new ClassSymbol(""), None, Set()).setType(tpe)

  private def createMethodId(methodSymbol: MethodSymbol): MethodID = MethodID(methodSymbol.name).setSymbol(methodSymbol)

  def stringConcat(exprTree: ExprTree, rest: ExprTree*): ExprTree = {
    def concat(exprs: List[ExprTree]): ExprTree = exprs match {
      case x :: Nil  => x
      case x :: rest => Plus(x, concat(rest)).setType(String)
      case _         => ???
    }

    concat(exprTree :: rest.toList)
  }

  def createValDecl(idName: String, initExpression: ExprTree, prefix: String = "$"): VarDecl =
    _createVarDecl(Set[Modifier](Private(), Final()), idName, initExpression, prefix)

  def createVarDecl(idName: String, initExpression: ExprTree, prefix: String = "$"): VarDecl =
    _createVarDecl(Set[Modifier](Private()), idName, initExpression, prefix)

  private def _createVarDecl(modifiers: Set[Modifier], idName: String, initExpression: ExprTree, prefix: String) = {
    val tpe = initExpression.getType
    if (tpe == TUntyped)
      sys.error("Cannot create var decl from an untyped initial expression.")

    initExpression.setType(tpe)
    val name = s"$prefix$idName"
    val id = VariableID(name)
    val varDecl = VarDecl(id, None, Some(initExpression), modifiers)
    val symbol = new VariableSymbol(idName)
    symbol.setType(tpe)
    varDecl.setSymbol(symbol)
    id.setSymbol(symbol)
    id.setType(tpe)
    varDecl
  }

  def createOne(t: Type): ExprTree = t match {
    case _ if t == Int    => IntLit(1)
    case _ if t == Char   => IntLit(1)
    case _ if t == Long   => LongLit(1l)
    case _ if t == Float  => FloatLit(1.0f)
    case _ if t == Double => DoubleLit(1.0)
    case _                => ???
  }

  def getTypeTree(tpe: Type): TypeTree = (tpe match {
    case TUnit                => UnitType()
    case TArray(t)            => ArrayType(getTypeTree(t))
    case TObject(classSymbol) => ClassID(classSymbol.name).setSymbol(classSymbol)
    case _                    => ???
  }).setType(tpe)

  def getCode: GeneratedExpr = {
    val generatedExpr = GeneratedExpr(code.toList).setPos(code.head)
    val tpe = code.last match {
      case typed: Typed => typed.getType
      case _            => TUnit
    }
    code.clear()
    generatedExpr.setType(tpe)
  }

  def setPos(pos: Positioned): Unit = code.foreach(_.setPos(pos))
}




