package game

import org.scalatest.FunSuite
import org.scalatest.prop.Checkers
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary._
import game.Shape.ops._

class ShapeTests extends FunSuite with Checkers {
  implicit def arbitraryShape[P: Shape]: Arbitrary[P] =
    Arbitrary(Gen.oneOf(Shape[P].all))

  def neighboursAreInAll[P: Shape](point: P): Boolean =
    point.neighbours.toSet.subsetOf(Shape[P].all.toSet)

  def neighboursAreUnique[P: Shape](point: P): Boolean =
    point.neighbours.distinct.size == point.neighbours.size

  test("Point19") {
    check(neighboursAreInAll[Point19] _)
    check(neighboursAreUnique[Point19] _)
  }
}
