package tlang.messages

import org.scalatest.{FreeSpec, Matchers}
import tlang.compiler.analyzer._
import tlang.compiler.ast.ParsingErrors
import tlang.compiler.ast.Trees.{Modifiable, Private}
import tlang.compiler.imports.ImportErrors
import tlang.compiler.lexer.Tokens.{INTLIT, INTLITKIND}
import tlang.compiler.lexer.{LexingErrors, Token, TokenKind}
import tlang.compiler.modification.TemplatingErrors
import tlang.formatting.{DefaultFormatting, Formatter}
import tlang.testutils.TestConstants.CompilerIntegrationTestTag
import tlang.testutils.snapshot.SnapshotTesting
import tlang.utils.Extensions._
import tlang.utils._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._

class MessageSnapshotSuite extends FreeSpec with Matchers with SnapshotTesting {

  override val suiteName: String = "Message Snapshots"


  private val CompilerMessageType = typeOf[CompilerMessage].typeSymbol

  val _errorStringContext = ErrorStringContext(Formatter(DefaultFormatting))
  val _reporter           = VoidReporter()


  testMessages(
    new LexingErrors {
      override protected var line              : Int                = 0
      override protected var column            : Int                = 0
      override protected var source            : Source             = StringSource("ABC", "Test")
      override           val reporter          : Reporter           = _reporter
      override           val errorStringContext: ErrorStringContext = _errorStringContext
    }
  )

  testMessages(
    new ParsingErrors {
      override val reporter          : Reporter           = _reporter
      override val errorStringContext: ErrorStringContext = _errorStringContext
    }
  )

  testMessages(
    new ImportErrors {
      override val reporter          : Reporter           = _reporter
      override val errorStringContext: ErrorStringContext = _errorStringContext
    }
  )

  testMessages(
    new TemplatingErrors {
      override val reporter          : Reporter           = _reporter
      override val errorStringContext: ErrorStringContext = _errorStringContext
    }
  )

  testMessages(
    new NamingErrors {
      override val reporter          : Reporter           = _reporter
      override val errorStringContext: ErrorStringContext = _errorStringContext
    }
  )

  testMessages(
    new TypingErrors {
      override val reporter          : Reporter           = _reporter
      override val errorStringContext: ErrorStringContext = _errorStringContext
    }
  )

  testMessages(
    new FlowingErrors {
      override val reporter          : Reporter           = _reporter
      override val errorStringContext: ErrorStringContext = _errorStringContext
    }
  )


  def testMessages[T <: ErrorHandling : ClassTag : TypeTag](enclosingClass: T): Unit = {
    val errorMessageClass = messageClass(enclosingClass)
    val typeMirror = runtimeMirror(enclosingClass.getClass.getClassLoader)
    val instanceMirror = typeMirror.reflect(enclosingClass)

    val testName = errorMessageClass.name.toString taggedAs CompilerIntegrationTestTag
    testName in {
      compilerMessageClasses(errorMessageClass) foreach { messageClass =>
        test(messageClass.name.toString) {
          val message = createMessage(instanceMirror, messageClass)
          s"${ message.code }: ${ message.message }" should matchSnapshot
        }
      }
    }
  }

  private def messageClass[T <: ErrorHandling : ClassTag : TypeTag](enclosingClass: T): universe.Symbol =
    typeOf[T]
      .baseClasses
      .find(clazz => clazz.isAbstract && clazz.name.toString.endsWith("Errors"))
      .get

  private def compilerMessageClasses(errorMessageClass: Symbol): Iterable[universe.ClassSymbol] = {
    errorMessageClass.typeSignature.decls.filter(isCompilerMessage).map(_.asClass)
  }

  private def isCompilerMessage(decl: Symbol): Boolean = {
    decl.typeSignature.baseClasses.contains(CompilerMessageType) && decl.isClass && !decl.isAbstract
  }

  private def createMessage(instanceMirror: InstanceMirror, messageClass: ClassSymbol): CompilerMessage = {
    val constructor = getConstructor(messageClass)
    val args = arguments(constructor)
    try {
      instanceMirror
        .reflectClass(messageClass)
        .reflectConstructor(constructor)
        .apply(args: _*)
        .asInstanceOf[CompilerMessage]
    } catch {
      case e: Throwable =>
        val argumentDescription =
          constructor.paramLists
            .flatten
            .zip(args)
            .map { case (expected, found) => s"${ expected.typeSignature } -> $found" }

        fail(
          s"""|Could not construct message ${ messageClass.name } with parameters:
              |   ${ argumentDescription.mkString("\n   ") }
              |
              |${ e.stackTrace }
            """.stripMargin)
    }

  }

  private def getConstructor(clazz: ClassSymbol): universe.MethodSymbol = {
    clazz.typeSignature.members.filter(m => m.isMethod && m.isConstructor).head.asMethod
  }

  private def arguments(constructor: universe.MethodSymbol) = constructor.paramLists.flatMap(_.map(valueForArgument))

  private val classSymbol    = new Symbols.ClassSymbol("ABC")
  private val methodSymbol   = new Symbols.MethodSymbol("ABC", classSymbol, None, Set(Private()))
  private val variableSymbol = new Symbols.VariableSymbol("ABC")
  // These values are used to construct the error messages.
  private def valueForArgument(arg: Symbol) = {
    arg.typeSignature match {
      case a if a <:< typeOf[Int]                            => 0
      case a if a <:< typeOf[String]                         => "ABC"
      case a if a <:< typeOf[Char]                           => 'A'
      case a if a <:< typeOf[Boolean]                        => true
      case a if a <:< typeOf[Token]                          => new INTLIT(0)
      case a if a <:< typeOf[TokenKind]                      => INTLITKIND
      case a if a <:< typeOf[Seq[_]]                         => Nil
      case a if a <:< typeOf[Set[_]]                         => Set()
      case a if a <:< typeOf[Symbols.ClassSymbol]            => classSymbol
      case a if a <:< typeOf[Symbols.MethodSymbol]           => methodSymbol
      case a if a <:< typeOf[Symbols.VariableSymbol]         => variableSymbol
      case a if a <:< typeOf[Symbols.Symbol with Modifiable] => methodSymbol
      case a if a <:< typeOf[Types.Type]                     => Types.Int
      case a if a <:< typeOf[Positioned]                     => tlang.utils.NoPosition
    }
  }

}
