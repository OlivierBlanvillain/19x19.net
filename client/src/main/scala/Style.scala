package client

import scalacss.Defaults._
import scalacss.ext.CssReset.normaliseCss

object Style extends StyleSheet.Inline {
  import dsl._

  val cssReset = style(normaliseCss)

  val body = style(
    unsafeRoot("body")(
      margin(20.px),
      overflowY.scroll))

  val board = new BoardStyle
}
