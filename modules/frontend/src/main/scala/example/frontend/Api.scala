package example.frontend

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.raquo.airstream.signal.Signal
import example.shared.Protocol._
import sttp.client._
import sttp.client.circe._

object Api {
  implicit val backend = FetchBackend()

  val ApiHost = {
    import org.scalajs.dom

    val scheme = dom.window.location.protocol
    val host = dom.window.location.host

    s"$scheme//$host"
  }

  def post(search: String, prefixOnly: Boolean = false) = {

    val req = basicRequest
      .post(uri"$ApiHost/get-suggestions")
      .body(GetSuggestions.Request(search, Some(prefixOnly)))
      .response(asJson[GetSuggestions.Response])

    val freq = req.send[Future].map(_.body)

    Signal.fromFuture(freq)
  }
}
