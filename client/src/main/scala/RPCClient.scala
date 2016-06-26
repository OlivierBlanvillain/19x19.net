package nineteen.client

import java.nio.ByteBuffer
import nineteen.model._
import nineteen.model.CachedPicklers._
import monixwire._
import scala.scalajs.js.typedarray.TypedArrayBufferOps._
import org.scalajs.dom.raw.MessageEvent
import org.scalajs.dom.{CloseEvent, ErrorEvent, Event, WebSocket}
import scala.scalajs.js.typedarray.{TypedArrayBuffer, ArrayBuffer}

class RPCClient
    extends BoopickleSerializers[Request, Response]
    with ObservableClient[ByteBuffer, Request, Response] {

  val webSocket = new WebSocket("ws://localhost:8080/ws")
  webSocket.binaryType = "arraybuffer"

  webSocket.onopen = (event: Event) =>
    println("webSocket.onopen = (event: Event) =>")

  webSocket.onerror = (event: ErrorEvent) =>
    println("webSocket.onerror = (event: ErrorEvent) =>")

  webSocket.onclose = (event: CloseEvent) =>
    println("webSocket.onclose = (event: CloseEvent) =>")

  webSocket.onmessage = (event: MessageEvent) =>
    networkReceive(TypedArrayBuffer.wrap(event.data.asInstanceOf[ArrayBuffer]))

  def networkSend(data: ByteBuffer): Unit =
    webSocket.send(data.arrayBuffer)
}
