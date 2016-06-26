package server

import java.nio.ByteBuffer
import model._
import model.CachedPicklers._
import monix.reactive.Observable
import monixwire._
import org.http4s.websocket.WebsocketBits.{Binary, WebSocketFrame}
import scalaz.stream.async.mutable.Queue
import scalaz.stream.async.unboundedQueue
import scalaz.stream.Exchange
import scala.Function.const

class RPCServer
    extends BoopickleSerializers[Request, Response]
    with ObservableServer[ByteBuffer, Request, Response] {

  private val in:  Queue[ByteBuffer] = unboundedQueue[ByteBuffer]
  private val out: Queue[ByteBuffer] = unboundedQueue[ByteBuffer]

  val exchange: Exchange[WebSocketFrame, WebSocketFrame] = Exchange(
    out.dequeue.map(x => Binary(x.array(), true)),
    in .enqueue.map(f => {
      case Binary(x, _) => f(ByteBuffer.wrap(x))
      case e => throw new RuntimeException(s"Unexpected WebSocketFrame: $e")
    })
  )

  in.dequeue.collect(PartialFunction(this.networkReceive _))
    .run.unsafePerformAsync(const(()))

  def networkSend(data: ByteBuffer): Unit =
    out.enqueueOne(data).unsafePerformAsync(const(()))

  def handle[C <: Request, R <: Response](c: C { type Res = R }): Observable[R] =
    c match {
      case x: Foo => ((Controller.foo _).tupled)(Foo.unapply(x).get).map(FooResponse.apply)
      case x: Bar => ((Controller.bar _).tupled)(Bar.unapply(x).get).map(BarResponse.apply)
    }
}
