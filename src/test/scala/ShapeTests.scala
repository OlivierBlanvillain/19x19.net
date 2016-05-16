package game

import game.Shape.ops._
import org.scalacheck.Arbitrary._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

class ShapeTests extends FunSuite with Checkers {
  implicit def arbitraryShape[P: Shape]: Arbitrary[P] =
    Arbitrary(Gen.oneOf(Shape[P].all))

  def neighboursAreInAll[P: Shape](point: P): Boolean =
    point.neighbours.toSet.subsetOf(Shape[P].all.toSet)

  def neighboursAreUnique[P: Shape](point: P): Boolean =
    point.neighbours.distinct.size == point.neighbours.size

  test("Point5") {
    check(neighboursAreInAll[Point5] _)
    check(neighboursAreUnique[Point5] _)
  }

  test("Point19") {
    check(neighboursAreInAll[Point19] _)
    check(neighboursAreUnique[Point19] _)
  }
}
