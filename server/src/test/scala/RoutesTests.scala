package server

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.FunSuite

class RoutesTests extends FunSuite with ScalatestRouteTest {
  test("Static routes") {
    Get("/") ~> Routes() ~> check {
      assert(responseAs[String].contains("doctype"))
    }

    Get("/index.html") ~> Routes() ~> check {
      assert(responseAs[String].contains("doctype"))
    }
  }
}
