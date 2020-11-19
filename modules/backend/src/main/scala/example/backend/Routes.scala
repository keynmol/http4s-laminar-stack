package example.backend

import scala.concurrent.duration._

import cats.effect._
import example.shared.Protocol._
import org.http4s.HttpRoutes
import org.http4s.StaticFile
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.io._

class Routes(service: Service, blocker: Blocker, frontendJS: String)(implicit
    timer: Timer[IO],
    cs: ContextShift[IO]
) {
  def routes = HttpRoutes.of[IO] {
    case request @ POST -> Root / "get-suggestions" =>
      for {
        req    <- request.as[GetSuggestions.Request]
        result <- service.getSuggestions(req)
        // introduce a fake delay here to showcase the amazing
        // loader gif
        resp <- Ok(result) <* timer.sleep(50.millis)
      } yield resp

    case request @ GET -> Root / "frontend" / "app.js" =>
      StaticFile
        .fromResource[IO](frontendJS, blocker, Some(request))
        .getOrElseF(NotFound())

    case request @ GET -> Root / "frontend" =>
      StaticFile
        .fromResource[IO]("index.html", blocker, Some(request))
        .getOrElseF(NotFound())

    case request @ GET -> Root / "assets" / path if staticFileAllowed(path) =>
      StaticFile
        .fromResource("/assets/" + path, blocker, Some(request))
        .getOrElseF(NotFound())
  }

  private def staticFileAllowed(path: String) =
    List(".gif", ".js", ".css", ".map", ".html", ".webm").exists(path.endsWith)

}
