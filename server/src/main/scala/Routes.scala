package server

import akka.http.scaladsl.server.{Directives, Route}

object Routes extends Directives {
  def apply(): Route =
    pathSingleSlash(getFromResource("content/index.html")) ~
    path(Segment)(s => getFromResource(s"content/$s")) ~
    path("target" / Segment)(s => getFromResource(s"content/target/$s"))
}
