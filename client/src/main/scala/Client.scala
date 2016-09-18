package client

import game._
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding.Var
import scala.scalajs.js.JSApp
import org.scalajs.dom.document

object Client extends JSApp {
  val movesOptimal34: Seq[Point19] = Seq(
    (3, 4), (3, 3), (4, 3), (4, 4), (4, 5),
    (2, 4), (5, 4), (3, 2), (2, 5), (1, 4),
    (4, 2), (3, 1), (1, 2), (2, 2), (3, 5),
    (1, 3), (4, 1), (1, 1), (1, 5)
  ).map(Function.tupled(Point19.apply))

  val positionOptimal34 = Go.playAll(movesOptimal34.map(Move.apply)).right.get.head

  val position = Var[Position[Point19]](positionOptimal34)

  def main(): Unit = {
    dom.render(document.body, Board(position))
  }
}
