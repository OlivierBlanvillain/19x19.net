package game

import scala.Function.unlift
import simulacrum.typeclass

/** Shape of a Go board. Should obey the law described in ShapeTests. */
@typeclass trait Shape[A] {
  def neighbours(a: A): Seq[A]
  def all: Seq[A]
}

object Shape {
  def unsafeIsShape[A](n: Int)(ap: (Int, Int) => A, up: A => (Int, Int)): Shape[A] =
    new Shape[A] {
      def neighbours(a: A): Seq[A] = {
        val (x, y) = up(a)
        List((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1))
          .collect {
            case (a, b) if a >= 1 && a <= n && b >= 1 && b <= n =>
              ap(a, b)
          }
      }

      def all: Seq[A] =
        for { i <- 1 to n; j <- 1 to n } yield ap(i, j)
    }

  implicit val point19IsShape: Shape[Point19] =
    unsafeIsShape(19)(Point19.apply, unlift(Point19.unapply))
}
