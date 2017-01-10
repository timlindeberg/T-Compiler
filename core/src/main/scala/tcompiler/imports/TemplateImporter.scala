package tcompiler.imports

import java.io.File

import tcompiler.Main
import tcompiler.ast.Parser
import tcompiler.ast.Trees._
import tcompiler.lexer.Lexer
import tcompiler.utils.{CompilationException, Context}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by Tim Lindeberg on 5/15/2016.
  */

object TemplateImporter {

  val importedFiles = mutable.Map[String, File]()

}

class TemplateImporter(ctx: Context,
  imported: mutable.Set[String] = mutable.Set()) {

  import TemplateImporter._

  def classExists(importName: String) = findClassFile(importName).isDefined

  def findClassFile(importName: String): Option[File] = {
    val fileName = importName.replaceAll("\\.", "/") + Main.FileEnding
    importedFiles.get(fileName) match {
      case Some(f) => Some(f)
      case None    =>
        ctx.getClassPaths foreach { path =>
          val file = new File(s"$path/$fileName")
          if (file.exists()) {
            importedFiles(fileName) = file
            return Some(file)
          }
        }
        None
    }
  }


  def importCus(importName: String): List[CompilationUnit] = {
    if (imported(importName))
      return Nil

    imported += importName
    findClassFile(importName) match {
      case Some(file) =>
        parseGenericFile(ctx, file) match {
          case Some(importedCU) =>
            // Recursively import generics
            val importedCUs: ArrayBuffer[CompilationUnit] = ArrayBuffer(importedCU)
            importedCU.importMap.importNames foreach { recursiveImport =>
              val templateImporter = new TemplateImporter(ctx, imported)
              importedCUs ++= templateImporter.importCus(recursiveImport)
            }
            importedCUs.toList
          case None               => Nil
        }
      case None       => Nil
    }
  }

  private def parseGenericFile(ctx: Context, file: File): Option[CompilationUnit] =
    try {
      val parsedProgram = (Lexer andThen Parser).run(ctx)(List(file)).head
      Some(parsedProgram)
    } catch {
      case e: CompilationException =>
        println(e.getMessage)
        None
    }
}