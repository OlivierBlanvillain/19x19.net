package nineteen.client

import java.nio.ByteBuffer
import org.scalajs.dom.raw.MessageEvent
import org.scalajs.dom.{CloseEvent, ErrorEvent, Event, WebSocket}
import scala.scalajs.js.Dynamic.global
import scala.scalajs.js.typedarray.TypedArrayBufferOps._
import scala.scalajs.js.typedarray.{TypedArrayBuffer, ArrayBuffer}

import monixwire._
import nineteen.model._
import nineteen.model.CachedPicklers._
class RPCClient
    extends BoopickleSerializers[Request, Response]
    with ObservableClient[ByteBuffer, Request, Response] {

  private val url: String = s"ws://${global.window.location.host}/${Routes.websocket}"
  private val webSocket = new WebSocket(url)
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
