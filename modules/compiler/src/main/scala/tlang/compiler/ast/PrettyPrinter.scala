package tlang
package compiler
package ast

import java.util.regex.Matcher

import tlang.compiler.imports.Imports
import tlang.compiler.lexer.Tokens
import tlang.formatting.Colors.Color
import tlang.formatting.Formatter

case class PrettyPrinter()(implicit formatter: Formatter) {

  import Trees._
  import formatter._

  private var currentIndent: Int = 0
  private val seperator = NL * 2 + "/* ----------------------------------------------------------------- */" + NL * 2

  def apply(ts: Traversable[Tree]): String = ts.map(apply).mkString(seperator)
  def apply(t: Tree): String = {
    currentIndent = 0
    prettyPrint(t).replaceAll("\n\t+\n", "\n\n")
  }

  private def prettyPrint(t: Tree): String = t match {
    case CompilationUnit(pack, classes, imps) => pp"$pack${ imports(imps) }$classes"
    // Imports
    case Package(address)        => pp"${ packDecl(address) }"
    case RegularImport(address)  => pp"import ${ address.mkString("::") }"
    case WildCardImport(address) => pp"import ${ address.mkString("::") }.*"
    // Class Declarations
    case ClassDecl(id, parents, fields, methods, annotations)  => classDecl(pp"class", annotations, id, parents, fields, methods)
    case TraitDecl(id, parents, fields, methods, annotations)  => classDecl(pp"trait", annotations, id, parents, fields, methods)
    case ExtensionDecl(id, extendedType, methods, annotations) => classDecl(pp"extension", annotations, id, extendedType :: Nil, Nil, methods)
    case AnnotationDecl(id, methods, annotations)              => classDecl(pp"annotation", annotations, id, Nil, Nil, methods)
    // Variable and method declarations
    case VarDecl(id, tpe, expr, modifiers, annos)                          =>
      pp"${ annotations(annos) }${ varDecl(modifiers) } $id${ optional(tpe) { t => pp": $t" } }${ optional(expr) { t => pp" = $t" } }"
    case MethodDecl(id, modifiers, annos, args, retType, stat)             =>
      pp"${ annotations(annos) }${ definition(modifiers) } $id(${ commaSeparated(args) })${ optional(retType) { t => pp": $t" } }${ optional(stat) { s => pp" = $s" } }"
    case ConstructorDecl(_, modifiers, annos, args, _, stat)               =>
      pp"${ annotations(annos) }${ definition(modifiers) } new(${ commaSeparated(args) }) = $stat"
    case OperatorDecl(operatorType, modifiers, annos, args, retType, stat) =>
      pp"${ annotations(annos) }${ definition(modifiers) } ${ operatorType.opSign }(${ commaSeparated(args) })${ optional(retType) { t => pp": $t" } } = $stat"
    // Modifiers
    case Formal(tpe, id, annos) => pp"${ annotations(annos) }$id: $tpe"
    case KeyValuePair(id, expr) => pp"$id = $expr"
    case Private()              => pp"private"
    case Public()               => pp"public"
    case Protected()            => pp"protected"
    case Final()                => pp"final"
    case Static()               => pp"static"
    case Implicit()             => pp"implicit"
    case Annotation(id, values) => pp"@$id${ optional(values) { v => pp"(${ commaSeparated(v) })" } }"
    // Types
    case ArrayType(tpe)    => pp"$tpe[]"
    case UnitType()        => pp"Unit"
    case NullableType(tpe) => pp"$tpe?"
    // Statements
    case Block(stats)                      => if (stats.isEmpty) ";" else pp"$L$stats$R"
    case If(condition, thn, els)           => pp"if($condition) ${ Stat(thn) }${ optional(els) { stat => pp"${ N }else ${ Stat(stat) }" } }"
    case While(condition, stat)            => pp"while($condition) ${ Stat(stat) }"
    case For(init, condition, post, stat)  => pp"for(${ commaSeparated(init) } ; $condition ; ${ commaSeparated(post) }) ${ Stat(stat) }"
    case Foreach(varDecl, container, stat) => pp"for($varDecl in $container) ${ Stat(stat) }"
    case Print(expr)                       => pp"print($expr)"
    case Println(expr)                     => pp"println($expr)"
    case Error(expr)                       => pp"error($expr)"
    case Assign(id, expr)                  => pp"$id = $expr"
    case Return(expr)                      => pp"return $expr"
    // Expressions
    case And(lhs, rhs)                     => pp"($lhs && $rhs)"
    case Or(lhs, rhs)                      => pp"($lhs || $rhs)"
    case Plus(lhs, rhs)                    => pp"($lhs + $rhs)"
    case Minus(lhs, rhs)                   => pp"($lhs - $rhs)"
    case LogicAnd(lhs, rhs)                => pp"($lhs & $rhs)"
    case LogicOr(lhs, rhs)                 => pp"($lhs | $rhs)"
    case LogicXor(lhs, rhs)                => pp"($lhs ^ $rhs)"
    case LeftShift(lhs, rhs)               => pp"($lhs << $rhs)"
    case RightShift(lhs, rhs)              => pp"($lhs >> $rhs)"
    case Times(lhs, rhs)                   => pp"($lhs * $rhs)"
    case Div(lhs, rhs)                     => pp"($lhs / $rhs)"
    case Modulo(lhs, rhs)                  => pp"($lhs % $rhs)"
    case LessThan(lhs, rhs)                => pp"($lhs < $rhs)"
    case LessThanEquals(lhs, rhs)          => pp"($lhs <= $rhs)"
    case GreaterThan(lhs, rhs)             => pp"($lhs > $rhs)"
    case GreaterThanEquals(lhs, rhs)       => pp"($lhs >= $rhs)"
    case Equals(lhs, rhs)                  => pp"($lhs == $rhs)"
    case NotEquals(lhs, rhs)               => pp"($lhs != $rhs)"
    case Is(expr, id)                      => pp"($expr is $id)"
    case As(expr, tpe)                     => pp"($expr as $tpe)"
    case Not(expr)                         => pp"!($expr)"
    case Negation(expr)                    => pp"-($expr)"
    case LogicNot(expr)                    => pp"~($expr)"
    case Hash(expr)                        => pp"#($expr)"
    case ArrayRead(arr, index)             => pp"$arr[$index]"
    case ArraySlice(arr, start, end, step) => pp"$arr[$start:$end:$step]"
    case NormalAccess(obj, application)    => access(obj, application, ".")
    case SafeAccess(obj, application)      => access(obj, application, "?.")
    case MethodCall(meth, args)            => pp"$meth(${ commaSeparated(args) })"
    case IntLit(value)                     => pp"$value"
    case LongLit(value)                    => pp"${ value }L"
    case FloatLit(value)                   => pp"${ value }F"
    case DoubleLit(value)                  => pp"$value"
    case CharLit(value)                    => pp"'${ escapeChar(pp"$value") }'"
    case StringLit(value)                  => "\"" + pp"${ escapeString(pp"$value") }" + "\""
    case ArrayLit(expressions)             => pp"[ ${ commaSeparated(expressions) } ]"
    case TrueLit()                         => pp"true"
    case FalseLit()                        => pp"false"
    case NullLit()                         => pp"null"
    case id@ClassID(value, _)              => pp"$value${ templateList(id) }"
    case Identifier(value)                 => pp"$value"
    case This()                            => pp"this"
    case Super(spec)                       => pp"super${ optional(spec) { s => pp"<$s>" } }"
    case NewArray(tpe, sizes)              => pp"new ${ newArray(tpe, sizes) }"
    case New(tpe, exprs)                   => pp"new $tpe(${ commaSeparated(exprs) })"
    case PreIncrement(id)                  => pp"++$id"
    case PostIncrement(id)                 => pp"$id++"
    case PreDecrement(id)                  => pp"--$id"
    case PostDecrement(id)                 => pp"$id--"
    case Ternary(condition, thn, els)      => pp"($condition ? $thn : $els)"
    case Elvis(nullableValue, ifNull)      => pp"($nullableValue ?: $ifNull)"
    case ExtractNullable(expr)             => pp"$expr!!"
    case Break()                           => pp"break"
    case Continue()                        => pp"continue"
    case Empty()                           => pp"<EMPTY>"
    case GeneratedExpr(stats)              => pp"${ genExpr(stats) }"
    case PutOnStack(expr)                  => s"<PutOnStack(${ pp"$expr" })>"
  }

  private val charEscapeChars = Map('\t' -> "t", '\b' -> "b", '\n' -> "n", '\r' -> "r", '\f' -> "f", '\\' -> "\\")
  private def escapeChar(str: String) = str.escape(charEscapeChars)
  private def escapeString(str: String) = str.escape

  private def access(obj: ExprTree, application: ExprTree, dotNotation: String) = obj match {
    case Empty() => pp"$application"
    case _       => pp"$obj$dotNotation$application"
  }

  private def classDecl(name: String, annos: List[Annotation], tpe: TypeTree, parents: List[ClassID], fields: List[VarDecl], methods: List[MethodDeclTree]): String = {
    val start = pp"${ N }${ annotations(annos) }$name $tpe${ parentList(parents) }"
    if (fields.isEmpty && methods.isEmpty)
      return start

    if (fields.isEmpty)
      return pp"$start = $L$methods$R"

    if (methods.isEmpty)
      return pp"$start = $L$fields$R"

    pp"$start = $L$fields$N$methods$R"
  }

  private def imports(imps: Imports) = {
    if (imps.imports.isEmpty) ""
    else
      pp"${ Separated(imps.imports, N) }$N"
  }

  private def genExpr(stats: List[StatTree]) = {
    if (stats.lengthCompare(1) == 0) pp"<${ stats.head }>"
    else pp"<$L$stats$R>"
  }

  private def packDecl(address: List[String]) = {
    if (address.isEmpty) ""
    else
      pp"package ${ address.mkString("::") }$N"
  }

  private def parentList(parents: List[ClassID]) = {
    if (parents.isEmpty) ""
    else
      pp": ${ commaSeparated(parents) }"
  }

  private def annotations(annotations: List[Annotation]) = {
    if (annotations.isEmpty) ""
    else
      pp"$annotations$N"
  }

  private def newArray(tpe: TypeTree, sizes: List[ExprTree]) = {
    def str(tpe: TypeTree, sizes: List[ExprTree]): String =
      tpe match {
        case NullableType(t) => pp"${ str(t, sizes) }?"
        case ArrayType(t)    => pp"${ str(t, sizes.tail) }[${ sizes.head }]"
        case t               => pp"$t"
      }

    str(tpe, sizes.reverse)
  }

  private def templateList(id: ClassID) = if (id.isTemplated) pp"<${ commaSeparated(id.templateTypes) }>" else ""

  private def definition(modifiers: Set[Modifier]) = {
    val decl = modifiers.find(_.isInstanceOf[Accessibility]) match {
      case Some(access) => access match {
        case Private()   => pp"def"
        case Public()    => pp"Def"
        case Protected() => pp"def protected"
      }
      case None         => "<def>"
    }

    decl + mods(modifiers)
  }

  private def varDecl(modifiers: Set[Modifier]) = {
    val isFinal = modifiers.contains(Final())
    val decl = modifiers.find(_.isInstanceOf[Accessibility]) match {
      case Some(access) => access match {
        case Private() if isFinal   => pp"val"
        case Private()              => pp"var"
        case Public() if isFinal    => pp"Val"
        case Public()               => pp"Var"
        case Protected() if isFinal => pp"val protected"
        case Protected()            => pp"var protected"
      }
      case None         => "<def>"
    }

    decl + mods(modifiers)
  }

  private def mods(modifiers: Set[Modifier]): String = {
    val mods = modifiers
      .collect {
        case Static()   => pp"static"
        case Implicit() => pp"implicit"
      }
    if (mods.isEmpty)
      return ""

    " " + mods.mkString(" ")
  }

  private def optional[T](t: Option[T])(f: (T => String)): String = if (t.isDefined) f(t.get) else ""
  private def optional[T](list: List[T])(f: (List[T] => String)): String = if (list.nonEmpty) f(list) else ""

  //--------------------------------------------------------
  // Custom string context to enable pp-string interpolation
  //--------------------------------------------------------

  trait Formatter {
    def apply(): String
    override def toString: String = apply
  }

  object L extends Formatter {
    def apply(): String = {
      currentIndent += 1
      N()
    }
  }

  object R extends Formatter {
    def apply(): String = {
      currentIndent -= 1
      N()
    }
  }

  object N extends Formatter {
    def apply(): String = "\n" + "\t" * currentIndent
  }

  case class Stat(stat: StatTree) extends Formatter {
    def apply(): String = {
      stat match {
        case Block(_) => pp"$stat"
        case _        =>
          currentIndent += 1
          val s = pp"$N$stat"
          currentIndent -= 1
          s
      }
    }
  }

  def commaSeparated(list: List[Tree]) = Separated(list, ", ")

  case class Separated[T](list: List[Tree], seperator: T) extends Formatter {
    def apply(): String = list.map(t => pp"$t").mkString(seperator.toString)
  }

  implicit class PrettyPrinterContext(val sc: StringContext) {

    def pp(args: Any*): String = {
      val strings = sc.parts.iterator
      val expressions = args.iterator
      val sb = new StringBuilder(colorKeywords(strings.next))
      while (strings.hasNext) {
        sb ++= evaluate(expressions.next)
        sb ++= colorKeywords(strings.next)
      }
      sb.toString
    }

    private def evaluate(obj: Any): String = obj match {
      case f: Formatter  => f()
      case t: Tree       =>
        val color = getColor(t)
        color(prettyPrint(t))
      case Some(t: Tree) => evaluate(t)
      case None          => ""
      case l: List[_]    => mkString(l.asInstanceOf[List[Tree]]) // Only lists of trees should be used
      case s: String     => s
      case null          => "null" // This can happen when mocking
      case x             => x.toString
    }

    private def mkString(list: List[Tree]) = {
      val it = list.iterator
      var s = ""
      while (it.hasNext) {
        s += evaluate(it.next)
        if (it.hasNext)
          s += N()
      }
      s
    }

    private def getColor(t: Tree): Color = t match {
      case _: VariableID            => VarColor
      case _: MethodID              => MethodColor
      case _: ClassID | _: UnitType => ClassColor
      case _: StringLit |
           _: CharLit               => StringColor
      case _: NumberLiteral[_]      => NumColor
      case _                        => SymbolColor
    }

    private def colorKeywords(output: String): String = {
      if (!formatter.useColor)
        return output

      Tokens.KeywordsRegex.replaceAllIn(output, m => {
        Matcher.quoteReplacement(KeywordColor(m.group(1)))
      })
    }
  }

}
