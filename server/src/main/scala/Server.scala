package server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object Server extends App {
  println("Starting server...")

  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  Http().bindAndHandle(Routes(), "localhost", 8080)
    .map(s"Server online at http:/" + _.localAddress)
    .foreach(println)
}
