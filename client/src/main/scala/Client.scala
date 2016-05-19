import org.scalajs.dom
import scala.scalajs.js.JSApp

object Client extends JSApp {
  def main(): Unit = {
    val hi = "Hello world!"
    dom.document.title = hi
    println(hi)
  }
}
