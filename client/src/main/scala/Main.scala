package client

import japgolly.scalajs.react.ReactDOM
import monix.execution.Scheduler.Implicits.global
import org.scalajs.dom
import scala.scalajs.js.JSApp
import scalacss.Defaults._
import scalacss.ScalaCssReact._
import game._
import model._

object Main extends JSApp {
  def main(): Unit = {

    val client = new RPCClient

    client.call(Foo(1, 1000)).foreach(println)

    val movesOptimal34: Seq[Point19] = Seq(
      (3, 4), (3, 3), (4, 3), (4, 4), (4, 5),
      (2, 4), (5, 4), (3, 2), (2, 5), (1, 4),
      (4, 2), (3, 1), (1, 2), (2, 2), (3, 5),
      (1, 3), (4, 1), (1, 1), (1, 5)
    ).map(Function.tupled(Point19.apply))

    val position = Go.playAll(movesOptimal34.map(Move.apply)).right.get.head

    Style.addToDocument()
    val div = dom.document.body.appendChild(dom.document.createElement("div"))
    ReactDOM.render(Board(Style.board).component(position), div)

    ()
  }
}
