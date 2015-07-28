package tcompiler

import utils._
import java.io.File
import lexer.Lexer
import lexer.PrintTokens
import ast.Parser
import ast.ASTPrinterWithSymbols
import scala.collection.mutable.HashMap
import tcompiler.analyzer.NameAnalysis
import tcompiler.analyzer.TypeChecking
import tcompiler.code.CodeGeneration
import tcompiler.modification.{Imports, Templates}
import scala.sys.process._

object Main {
  val tokensFlag = "--tokens"
  val astFlag = "--ast"
  val symId = "--symid"
  val exec = "--exec"
  val flags = HashMap(tokensFlag -> false, astFlag -> false, symId -> false, exec -> false)

  def processOptions(args: Array[String]): Context = {

    val reporter = new Reporter()
    var outDir: Option[File] = None
    var files: List[File] = Nil

    def processOption(args: List[String]): Unit = args match {
      case "-d" :: out :: args =>
        outDir = Some(new File(out))
        processOption(args)

      case flag :: args if flags.contains(flag) =>
        flags(flag) = true
        processOption(args)

      case f :: args =>
        files = new File(f) :: files
        processOption(args)

      case Nil =>
    }

    processOption(args.toList)

    if (files.size != 1) reporter.fatal("Exactly one file expected, " + files.size + " file(s) given.")

    Context(reporter = reporter, file = files.head, outDir = outDir)
  }

  def main(args: Array[String]) {
    val ctx = processOptions(args)
    try {
      if (flags(tokensFlag)) {
        // Lex the program and print all tokens
        (Lexer andThen PrintTokens).run(ctx)(ctx.file).toList
      } else {
        val parsing = Lexer andThen Parser andThen Templates andThen Imports
        val analysis = NameAnalysis andThen TypeChecking
        if (flags(astFlag)) {
          if (flags(symId)) {
            // Run analysis and print AST tree with symbols
            val program = (parsing andThen analysis).run(ctx)(ctx.file)
            println(ASTPrinterWithSymbols(program))
          } else {
            // Parse and print AST tree
            val program = parsing.run(ctx)(ctx.file)
            println(program)
          }
        } else {
          // Generate code
          val prog = (parsing andThen analysis).run(ctx)(ctx.file)
          CodeGeneration.run(ctx)(prog)
          System.out.flush()
          if(flags(exec) && prog.main.isDefined){
            println(ctx.outDir match {
              case Some(dir) =>
                "java -cp " + ctx.outDir.get.getAbsolutePath + " kool.std." + prog.main.get.id.value !!
              case None      => "java " + prog.main.get.id.value !!
            })
          }
        }
      }
    } catch {
      // Reporter throws exception at fatal instead exiting program
      case e: CompilationException =>
    }
  }
}