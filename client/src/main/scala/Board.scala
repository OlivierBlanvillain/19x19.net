package client

import game._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{vdom, ReactComponentB, ReactComponentC}
import org.scalajs.dom.{raw, svg, html}
import scalacss.ScalaCssReact._

case class Board(style: BoardStyle) {
  val component: ReactComponentC.ReqProps[Position[Point19], Unit, Unit, raw.Element] =
    ReactComponentB[Position[Point19]]("").render_P(p => theBoard(p)).build

  def theBoard(position: Position[Point19]): vdom.ReactTagOf[html.Div] =
    <.div(style.board,
      goban,
      <.div(style.points,
        Shape[Point19].all.map(position.at).map {
          case Empty        => <.div(style.empty)
          case Stone(Black) => <.div(style.black)
          case Stone(White) => <.div(style.white)
        }
      )
    )

  val goban: vdom.ReactTagOf[svg.SVG] = {
    import japgolly.scalajs.react.vdom.svg.prefix_<^._

    val u = style.gridUnit
    val w = 18 * style.gridUnit
    val c = style.gridUnit / 2.0 + 0.5
    val gray = "#111"

    val viewBox =
      ^.viewBox := s"-$c, -$c, ${w + u}, ${w + u}"

    val border = <.rect(
      ^.height := w,
      ^.width := w,
      ^.stroke := gray,
      ^.strokeWidth := 1,
      ^.fill := "none")

    val hp = s"m 0 $u h  $w "
    val hm = s"m 0 $u h -$w "
    val vp = s"m $u 0 v  $w "
    val vm = s"m $u 0 v -$w "

    val grid = <.path(
      ^.stroke := gray,
      ^.strokeWidth := 1,
      ^.fill := "none",
      ^.d := s"M 0 0 h $w $hm  ${(hp + hm) * 8} M 0 0 v $w $vm ${(vp + vm) * 8}")

    val pr = s"m  ${u * 6} 0 l 0 0"
    val pl = s"m -${u * 6} 0 l 0 0"
    val pd = s"m 0  ${u * 6} l 0 0"

    val hoshis = <.path(
      ^.stroke := gray,
      ^.strokeWidth := 5,
      ^.strokeLinecap := "round",
      ^.d := s"m ${u * 3} ${u * 3} l 0 0 $pr $pr $pd $pl $pl $pd $pr $pr")

    <.svg(style.goban, viewBox, border, grid, hoshis)
  }
}
