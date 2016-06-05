package client

import org.scalatest.FunSuite

class StyleTests extends FunSuite {
  test("Styles have no conflicts") {
    assert(Style.styles.map(_.style.warnings).flatten.isEmpty)
  }
}
