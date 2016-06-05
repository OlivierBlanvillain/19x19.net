package client

import scalacss.Defaults._
import scalacss.Macros.Color
import scalacss.mutable.Register
import scalacss.{Length, Percentage}

class BoardStyle(implicit r: Register) extends StyleSheet.Inline()(r) {
  import dsl._

  private def size(length: Percentage[Int]) = mixin(width(length), height(length))
  private def size(length: Length[Int])     = mixin(width(length), height(length))
  private def size(length: String)          = mixin(width :=! length, height :=! length)

  val gameSize = 19
  val gridUnit = 36
  val boardSize = (gameSize * gridUnit).px

  val boardColor = c"#E4BB67"
  val blackColor = c"#333333"
  val whiteColor = c"#F3F3F3"

  val board = style(
    size(boardSize),
    backgroundColor(boardColor),
    border(8.px, solid, boardColor),
    position.relative,
    float.left,
    display.inline)

  val goban = style(
    position.absolute,
    zIndex(0))

  val points = style(
    size(boardSize),
    position.absolute,
    zIndex(2))

  val point = mixin(
    size(s"calc(100% / $gameSize - 2px)"),
    float.left,
    margin(1.px),
    borderRadius(50.%%))

  val empty = style(point)

  val black = style(point, backgroundColor(blackColor))

  val white = style(
    point,
    boxShadow := "2px 2px rgba(0, 0, 0, 0.6)",
    backgroundColor(whiteColor))

  private def pseudoCirle(c: Color) = mixin(
    content := "''",
    borderRadius(50.%%),
    display.inlineBlock,
    backgroundColor(c))

  private def hover(c: Color) = style(
    point,
    &.hover(&.before(
      size(100.%%),
      opacity(0.6),
      pseudoCirle(c))))

  val blackHover = hover(blackColor)
  val whiteHover = hover(whiteColor)

  private def last(c: Color) = style(
    &.before(
      size(30.%%),
      margin(35.%%),
      pseudoCirle(c)))

  val blackLast = last(whiteColor)
  val whiteLast = last(blackColor)
}
