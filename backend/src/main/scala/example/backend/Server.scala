package example.backend

import cats.effect._
import cats.implicits._
import org.http4s.dsl.io._
import org.http4s.circe._
import io.circe._
import org.http4s.HttpRoutes
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.implicits._
import example.shared.Protocol.GetSuggestions
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.StaticFile
import scala.concurrent.duration._

object Server extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {

    def routes(blocker: Blocker) = HttpRoutes.of[IO] {
      case request @ POST -> Root / "get-suggestions" =>
        for {
          req <- request.as[GetSuggestions.Request]
          result = Service.getSuggestions(req)
          // introduce a fake delay here to showcase the amazing
          // loader gif
          resp <- Ok(result) <* timer.sleep(200.millis)
        } yield resp

      case request @ GET -> Root / "frontend" / "dev.js" =>
        StaticFile
          .fromResource[IO]("dev.js", blocker, Some(request))
          .getOrElseF(NotFound())

      case request @ GET -> Root / "frontend" =>
        StaticFile
          .fromResource[IO]("index-dev.html", blocker, Some(request))
          .getOrElseF(NotFound())

      case request @ GET -> Root / "assets" / path if staticFileAllowed(path) =>
        StaticFile
          .fromResource("/assets/" + path, blocker, Some(request))
          .getOrElseF(NotFound())
    }

    val serverBuilder = BlazeServerBuilder[IO].bindHttp(8080, "localhost")

    val server = for {
      blocker <- Blocker.apply[IO]
      httpApp = routes(blocker)
      serverB <- serverBuilder.withHttpApp(httpApp.orNotFound).resource
    } yield ()

    server
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }

  private def staticFileAllowed(path: String) =
    List(".gif", ".js", ".css", ".map", ".html", ".webm").exists(path.endsWith)

}
