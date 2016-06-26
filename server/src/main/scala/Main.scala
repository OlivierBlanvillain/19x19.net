package nineteen.server

import java.io.File
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze._
import org.http4s.server.websocket.WS
import scalaz.concurrent.Task

import nineteen.model.Routes

object Main extends ServerApp {
  def server(args: List[String]): Task[Server] =
    BlazeBuilder.mountService(service).start

  val service: HttpService = HttpService {
    case GET -> Root / Routes.websocket =>
      WS(new RPCServer().exchange)

    case GET -> Root / "target" / segment =>
      fromContent(s"target/$segment")

    case _ =>
      fromContent("assets/index.html")
  }

  def fromContent(path: String): Task[Response] = {
    val response: Option[Response] =
      if (new File("LICENSE").exists)
        StaticFile.fromString(s"static/$path")    // Used in test
      else if (new File("../LICENSE").exists)
        StaticFile.fromString(s"../static/$path") // Used in run
      else
        StaticFile.fromResource(s"$path")         // Used in assembly

    response.fold(NotFound())(Task.now)
  }
}
