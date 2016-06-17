package monixwire

import monix.execution.Ack
import monix.execution.Cancelable
import monix.execution.Scheduler.Implicits.global
import monix.reactive.observers.SyncSubscriber
import monix.reactive.OverflowStrategy
import monix.reactive.{Observable, Observer}
import scala.collection.mutable
import scala.concurrent.Future
import boopickle.Default._
import java.nio.ByteBuffer

// Until https://github.com/OlivierBlanvillain/monixwire is published...

trait PathDependentResponse {
  type Out
  def r: Out
}

trait PathDependentRequest {
  type Res <: PathDependentResponse
  type Out = Res#Out
}

trait Serializers[PickleType, Request, Response] {
  protected def decode(s: PickleType): InternalMessage
  protected def encode(m: InternalMessage): PickleType

  protected sealed trait InternalMessage
  protected case class Next(request: Request, response: Response) extends InternalMessage
  protected case class Start(request: Request) extends InternalMessage
  protected case class Complete(request: Request) extends InternalMessage
}

trait ObservableClient[PickleType, Request <: PathDependentRequest, Response <: PathDependentResponse] {
    self: Serializers[PickleType, Request, Response] =>

  def networkSend(data: PickleType): Unit

  def networkReceive(data: PickleType): Unit =
    decode(data) match {
      case Next(request, response) =>
        val ignoredAck = map.get(request).get
          .asInstanceOf[SyncSubscriber[Any]]
          .onNext(response)

      case Complete(request) =>
        map.get(request).get.onComplete()

      case _ => ???
    }

  def overflowStrategy: OverflowStrategy.Synchronous[Nothing] = OverflowStrategy.Unbounded

  private val map: mutable.Map[Request, SyncSubscriber[_]] = mutable.Map.empty

  def call[C <: Request, R <: Response, T](c: C { type Res = R; type Out = T }): Observable[c.Out] =
    Observable.create[R { type Out = T }](overflowStrategy) { downstream =>
      map += (c -> downstream)
      networkSend(encode(Start(c)))
      Cancelable.empty
    }.map(_.r)
}

trait ObservableServer[PickleType, Request <: PathDependentRequest, Response <: PathDependentResponse] {
    self: Serializers[PickleType, Request, Response] =>

  def handle[C <: Request, R <: Response](c: C { type Res = R }): Observable[c.Res]

  def networkSend(data: PickleType): Unit

  def networkReceive(data: PickleType): Unit =
    decode(data) match {
      case Start(c) =>
        val observable = new Observer[Response] {
          def onNext(elem: Response): Future[Ack] = {
            networkSend(encode(Next(c, elem)))
            Ack.Continue
          }

          def onError(ex: Throwable): Unit =
            throw ex

          def onComplete(): Unit =
            networkSend(encode(Complete(c)))
        }

        val pdRequest = c.asInstanceOf[Request { type Res = Response }]
        val ignoredCancelable = handle(pdRequest).subscribe(observable)

      case _ => ???
    }

  protected implicit class TupledFunction1[A, B](f: Function1[A, B]) {
    def tupled = f
  }
}

abstract class BoopickleSerializers[Request: Pickler, Response: Pickler]
    extends Serializers[ByteBuffer, Request, Response] {

  private implicit val internalMessagePickler: Pickler[InternalMessage] =
    compositePickler[InternalMessage]
      .addConcreteType[Next]
      .addConcreteType[Start]
      .addConcreteType[Complete]

  def decode(b: ByteBuffer): InternalMessage = Unpickle[InternalMessage].fromBytes(b)
  def encode(m: InternalMessage): ByteBuffer = Pickle.intoBytes(m)
}
