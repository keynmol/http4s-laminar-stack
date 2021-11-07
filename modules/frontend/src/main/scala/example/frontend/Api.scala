package example.frontend

import cats.effect.IO
import cats.effect.unsafe.implicits.*

import scala.concurrent.Future

import example.shared.Protocol.*
import org.http4s.client.Client
import org.http4s.client.dsl.io.*
import org.http4s.Method.*
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dom.FetchClientBuilder
import org.http4s.syntax.all.*

trait Api:
  def post(
      search: String,
      prefixOnly: Boolean = false
  ): Future[Either[Throwable, GetSuggestions.Response]]

object FutureApi extends Api:
  private val client: Client[IO] = FetchClientBuilder[IO].create

  private def ApiHost =
    import org.scalajs.dom

    val scheme = dom.window.location.protocol
    val host   = dom.window.location.host

    Uri.unsafeFromString(s"$scheme//$host")

  def post(
      search: String,
      prefixOnly: Boolean = false
  ): Future[Either[Throwable, GetSuggestions.Response]] =
    client
      .expect[GetSuggestions.Response](
        POST(
          GetSuggestions.Request(search, Some(prefixOnly)),
          ApiHost / "get-suggestions"
        )
      )
      .attempt
      .unsafeToFuture()
  end post
end FutureApi
