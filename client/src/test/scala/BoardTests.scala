package nineteen.client

import nineteen.game._
import japgolly.scalajs.react.test._
import org.scalacheck.Arbitrary._
import org.scalacheck.Shapeless._
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

class BoardTests extends FunSuite with Checkers {
  test("Render right number of stones") {
    check { moves: Seq[Move[Point19]] =>
      Go.playAll(moves) match {
        case Left(_) | Right(Nil) => true
        case Right(position :: _) =>
          ComponentTester(Board(Style.board).component)(position) { tester =>
            val html: String = tester.component.outerHtmlWithoutReactDataAttr()
            val pieces: Seq[Color] = Shape[Point19].all.map(position.at)

            "BoardStyle-white".r.findAllMatchIn(html).length == pieces.count(Stone(White).==)
            "BoardStyle-black".r.findAllMatchIn(html).length == pieces.count(Stone(Black).==) &&
            "BoardStyle-empty".r.findAllMatchIn(html).length == pieces.count(Empty.==)
          }
      }
    }
  }
}
