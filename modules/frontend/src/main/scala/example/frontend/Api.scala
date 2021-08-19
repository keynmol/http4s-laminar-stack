package example.frontend

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import example.shared.Protocol.*
import sttp.client3.*
import sttp.client3.circe.*
import sttp.capabilities.WebSockets

trait Api:
  def post(
      search: String,
      prefixOnly: Boolean = false
  ): Future[Either[Throwable, GetSuggestions.Response]]

object FutureApi extends Api:
  given backend: SttpBackend[Future, WebSockets] = FetchBackend()

  private def ApiHost =
    import org.scalajs.dom

    val scheme = dom.window.location.protocol
    val host   = dom.window.location.host

    s"$scheme//$host"

  def post(
      search: String,
      prefixOnly: Boolean = false
  ): Future[Either[Throwable, GetSuggestions.Response]] =

    val req = basicRequest
      .post(uri"$ApiHost/get-suggestions")
      .body(GetSuggestions.Request(search, Some(prefixOnly)))
      .response(asJson[GetSuggestions.Response])

    req.send(backend).map(_.body)
end FutureApi
