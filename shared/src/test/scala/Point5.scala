package game

import scala.Function.unlift

case class Point5(x: Int, y: Int) {
  require(x >= 1 && x <= 5)
  require(y >= 1 && y <= 5)
}

object Point5 {
  implicit val point5IsShape: Shape[Point5] =
    Shape.unsafeIsShape(5)(Point5.apply, unlift(Point5.unapply))
}
