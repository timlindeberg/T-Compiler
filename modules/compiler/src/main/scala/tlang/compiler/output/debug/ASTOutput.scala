package tlang
package compiler
package output
package debug

import tlang.compiler.ast.Trees.Tree
import tlang.compiler.ast.{PrettyPrinter, TreePrinter}
import tlang.formatting.Formatter
import tlang.formatting.grid.Alignment.Center
import tlang.utils.JSON.Json

object ASTOutput {

  def apply(phaseName: String, trees: List[Tree])(implicit formatter: Formatter): ASTOutput = {
    val prettyPrinter = PrettyPrinter()
    val treePrinter = TreePrinter()
    ASTOutput(prettyPrinter, treePrinter, phaseName, trees)
  }
}

case class ASTOutput(
  prettyPrinter: PrettyPrinter,
  treePrinter: TreePrinter,
  phaseName: String,
  trees: List[Tree]
)(implicit formatter: Formatter) extends Output {

  override def pretty: String = {

    import formatter._

    val grid = formatter.grid.header(Bold("Output after ") + Blue(phaseName.capitalize) + Bold(" phase"))
    trees foreach { tree =>
      grid
        .row(alignment = Center)
        .content(tree.sourceDescription)
        .row()
        .content(replaceTabs(prettyPrinter(tree)).trimWhiteSpaces)
      treePrinter.addToGrid(grid, tree)
    }
    grid.render()
  }

  override def json: Json = Json()
}
