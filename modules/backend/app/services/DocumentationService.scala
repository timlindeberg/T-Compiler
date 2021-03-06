package services

import java.nio.file.{Files, Path, Paths}

import javax.inject.{Inject, _}
import play.api.Configuration
import tlang.formatting.Formatter
import tlang.options.FlagArgument

import scala.collection.JavaConverters._
import scala.io.Source

case class Documentation(name: String, content: String)

@Singleton
class DocumentationService @Inject()(config: Configuration) {

  private implicit val formatter: Formatter = Formatter.SimpleFormatter

  private lazy val compilerFlagDocs = flagDocumentation("Compiler flags", tlang.compiler.CompilerMain.Flags)
  private lazy val replFlagDocs = flagDocumentation("trepl flags", tlang.repl.ReplMain.Flags)

  def documentation: List[Documentation] = {
    staticDocumentation
  }

  private def staticDocumentation: List[Documentation] = {
    val documentationPath = Paths.get(config.get[String]("tlang.documentation.path"))
    Files.walk(documentationPath)
      .iterator
      .asScala
      .filter(_.toString.endsWith(".md"))
      .toList
      .sortBy { path => path.getFileName.toString.split("_").head.toInt }
      .map { path =>
        val source = Source.fromFile(path.toFile)
        val content = try source.mkString finally source.close()
        Documentation(markdownFileName(path), content)
      }
  }

  private def flagDocumentation(name: String, flags: Set[FlagArgument[_]]): Documentation = {
    val flagDocumentation = flags.map { flag =>
      s"""
         |### ${ flag.flagName }
         |${ flag.getExtendedDescription }
       """.stripMargin.trim
    }.mkString("\n\n")

    val content =
      s"""
         |# $name
         |
         |$flagDocumentation
     """.stripMargin.trim
    Documentation(name, content)
  }

  private def markdownFileName(path: Path): String = {
    val fileName = path.getFileName.toString
    fileName.replaceAll("""^(\d+)_""", "").replaceAll("""\.md$""", "")
  }
}
