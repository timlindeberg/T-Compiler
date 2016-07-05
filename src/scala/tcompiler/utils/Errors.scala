package tcompiler.utils

import tcompiler.imports.ImportMap

/**
  * Created by Tim Lindeberg on 5/13/2016.
  */
trait Errors {

  val ErrorPrefix: String
  var ctx        : Context
  var importMap  : ImportMap

  def warning(errorCode: Int, msg: String, pos: Positioned) =
    ctx.reporter.warning(ErrorPrefix, errorCode, msg, pos, importMap)

  def fatal(errorCode: Int, msg: String, pos: Positioned) =
    ctx.reporter.fatal(ErrorPrefix, errorCode, msg, pos, importMap)

}
