package game

import simulacrum.typeclass

/** Shape of a Go board. Should obey the law described in ShapeTests. */
@typeclass trait Shape[A] {
  def neighbours(a: A): Seq[A]
  def all: Seq[A]
}

object Shape {
  implicit val point19IsShape = new Shape[Point19] {
    def neighbours(p: Point19): Seq[Point19] =
      List((p.x + 1, p.y), (p.x - 1, p.y), (p.x, p.y + 1), (p.x, p.y - 1))
        .collect {
          case (a, b) if a >= 1 && a <= 19 && b >= 1 && b <= 19 =>
            Point19(a, b)
        }

    def all: Seq[Point19] =
      for { i <- 1 to 19; j <- 1 to 19 } yield Point19(i, j)
  }
}
