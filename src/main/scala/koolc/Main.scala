package koolc

import utils._
import java.io.File
import lexer.Lexer
import lexer.PrintTokens
import ast.Parser
import ast.ASTPrinterWithSymbols
import scala.collection.mutable.HashMap
import koolc.analyzer.NameAnalysis
import koolc.analyzer.TypeChecking
import koolc.code.CodeGeneration

object Main {
  val tokensFlag = "--tokens"
  val astFlag = "--ast"
  val symId = "--symid"
  val flags = HashMap(tokensFlag -> false, astFlag -> false, symId -> false)

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

    if (files.size != 1) {
      reporter.fatal("Exactly one file expected, " + files.size + " file(s) given.")
    }

    Context(reporter = reporter, file = files.head, outDir = outDir)
  }

  def main(args: Array[String]) {
    val ctx = processOptions(args)

    try {
      val ctx = processOptions(args)
      if (flags(tokensFlag)) {
        // Lex the program and print all tokens
        (Lexer andThen PrintTokens).run(ctx)(ctx.file).toList
      } else {
        var parsing = Lexer andThen Parser;
        var analysis = NameAnalysis andThen TypeChecking;
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
          (parsing andThen analysis andThen CodeGeneration).run(ctx)(ctx.file)
        }
      }
    } catch {
      // Reporter throws exception at fatal instead exiting program
      case e: Throwable => System.err.println(e.getMessage)
    }
  }
}
