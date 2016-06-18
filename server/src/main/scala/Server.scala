package server

import org.http4s._
import org.http4s.dsl._
import org.http4s.server.{ServerApp, Server => Http4sServer}
import org.http4s.server.blaze._
import scalaz.concurrent.Task
import java.io.File

object Server extends ServerApp {
  def server(args: List[String]): Task[Http4sServer] =
    BlazeBuilder.mountService(service).start

  val service: HttpService = HttpService {
    case GET -> Root =>
      fromContent("index.html")

    case GET -> Root / segment =>
      fromContent(segment)

    case GET -> Root / "target" / segment =>
      fromContent(s"target/$segment")
  }

  def fromContent(path: String): Task[Response] = {
    val response: Option[Response] =
      if (new File("LICENSE").exists)
        StaticFile.fromString(s"static/content/$path")    // Used in test
      else if (new File("../LICENSE").exists)
        StaticFile.fromString(s"../static/content/$path") // Used in run
      else
        StaticFile.fromResource(s"content/$path")         // Used in assembly

    response.fold(NotFound())(Task.now)
  }
}
