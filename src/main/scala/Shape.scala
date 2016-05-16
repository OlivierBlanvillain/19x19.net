package game

import simulacrum.typeclass
import scala.Function.unlift

/** Shape of a Go board. Should obey the law described in ShapeTests. */
@typeclass trait Shape[A] {
  def neighbours(a: A): Seq[A]
  def all: Seq[A]
}

object Shape {
  def unsafeIsShape[A](n: Int)(A: (Int, Int) => A, U: A => (Int, Int)): Shape[A] =
    new Shape[A] {
      def neighbours(a: A): Seq[A] = {
        val (x, y) = U(a)
        List((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1))
          .collect {
            case (a, b) if a >= 1 && a <= n && b >= 1 && b <= n =>
              A(a, b)
          }
      }

      def all: Seq[A] =
        for { i <- 1 to n; j <- 1 to n } yield A(i, j)
    }

  implicit val point19IsShape: Shape[Point19] =
    unsafeIsShape(19)(Point19.apply, unlift(Point19.unapply))
}
