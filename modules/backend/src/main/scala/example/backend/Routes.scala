package example.backend

import scala.concurrent.duration._

import cats.effect._

import org.http4s.HttpRoutes
import org.http4s.StaticFile
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._

import example.shared.Protocol._
import org.http4s.circe.CirceEntityDecoder
import cats._
import org.http4s.dsl.Http4sDsl

class Routes(
    service: Service,
    frontendJS: String
) extends Http4sDsl[IO]:
  def routes = HttpRoutes.of[IO] {
    case request @ POST -> Root / "get-suggestions" =>
      for {
        req <- circeEntityDecoder[IO, GetSuggestions.Request].decode(request, strict = true).value
        result <- service.getSuggestions(req.getOrElse(throw new RuntimeException("what")))
        // introduce a fake delay here to showcase the amazing
        // loader gif
        resp <- Ok(result) <* IO.sleep(50.millis)
      } yield resp

    case request @ GET -> Root / "frontend" / "app.js" =>
      StaticFile
        .fromResource[IO](frontendJS, Some(request))
        .getOrElseF(NotFound())

    case request @ GET -> Root / "frontend" =>
      StaticFile
        .fromResource[IO]("index.html", Some(request))
        .getOrElseF(NotFound())

    case request @ GET -> Root / "assets" / path if staticFileAllowed(path) =>
      StaticFile
        .fromResource("/assets/" + path, Some(request))
        .getOrElseF(NotFound())
  }

  private def staticFileAllowed(path: String) =
    List(".gif", ".js", ".css", ".map", ".html", ".webm").exists(path.endsWith)
