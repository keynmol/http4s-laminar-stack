package example.frontend

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import example.shared.Protocol._
import sttp.client3._
import sttp.client3.circe._

trait Api {
  def post(
      search: String,
      prefixOnly: Boolean = false
  ): Future[Either[Throwable, GetSuggestions.Response]]
}

object FutureApi extends Api {
  implicit val backend = FetchBackend()

  val ApiHost = {
    import org.scalajs.dom

    val scheme = dom.window.location.protocol
    val host   = dom.window.location.host

    s"$scheme//$host"
  }

  def post(
      search: String,
      prefixOnly: Boolean = false
  ): Future[Either[Throwable, GetSuggestions.Response]] = {

    val req = basicRequest
      .post(uri"$ApiHost/get-suggestions")
      .body(GetSuggestions.Request(search, Some(prefixOnly)))
      .response(asJson[GetSuggestions.Response])

    req.send(backend).map(_.body)
  }

}
