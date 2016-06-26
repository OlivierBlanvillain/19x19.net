package nineteen.model

import monixwire._
import boopickle.Default._

sealed trait Response                           extends PathDependentResponse
final case class FooResponse(r: Int)            extends Response { type Out = Int }
final case class BarResponse(r: Option[String]) extends Response { type Out = Option[String] }

sealed trait Request                 extends PathDependentRequest
final case class Foo(i: Int, j: Int) extends Request { type Res = FooResponse }
final case class Bar(s: String)      extends Request { type Res = BarResponse }

object CachedPicklers {
  implicit val callPickler: Pickler[Request] = generatePickler[Request]
  implicit val resultPickler: Pickler[Response] = generatePickler[Response]
}

object Routes {
  val websocket: String = "ws"
}
