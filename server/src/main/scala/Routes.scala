package server

import akka.http.scaladsl.server.{Directives, Route}
import java.io.File
object Routes extends Directives {
  def apply(): Route =
    pathSingleSlash(getFrom("content/index.html")) ~
    path(Segment)(s => getFrom(s"content/$s")) ~
    path("target" / Segment)(s => getFrom(s"content/target/$s"))

  def getFrom(content: String): Route =
    if (new File("../LICENSE").exists)
      getFromFile(s"../static/$content") // From sbt
    else
      getFromResource(content) // From a jar
}
