import game._
import org.scalacheck.{Gen, Arbitrary}

package object client {
  implicit val arbitraryPoint19: Arbitrary[Point19] = Arbitrary(
    for {
      x <- Gen.chooseNum(1, 19)
      y <- Gen.chooseNum(1, 19)
    } yield Point19(x, y)
  )
}
