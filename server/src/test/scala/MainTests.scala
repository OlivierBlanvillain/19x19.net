package nineteen.server

import org.scalatest.FunSuite
import org.http4s.server.Server
import org.http4s.client.blaze.SimpleHttp1Client
import org.http4s.server.blaze.BlazeBuilder

class MainTests extends FunSuite {
  def call(path: String): String =
    SimpleHttp1Client().expect[String](s"http://localhost:8080$path").unsafePerformSync

  def withServer[A](test: Server => A): A = {
    val server: Server = BlazeBuilder.mountService(Main.service).run
    try { test(server) } finally { server.shutdownNow }
  }

  test("Static routes") {
    withServer { server =>
      val root = ""
      val targets = Seq(
        "/target/client-jsdeps.min.js",
        "/target/client-opt.js",
        "/target/client-launcher.js")

      assert(call("/index.html") == call(root))
      assert(targets.forall(call(root).contains))
      assert(targets.map(call).forall(_.contains("typeof")))
    }
  }
}
