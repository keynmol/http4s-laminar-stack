package example.backend

import cats.effect._
import cats.implicits._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.GZip

object Server extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {

    def server(config: ServerConfig) =
      for {
        blocker <- Blocker.apply[IO]

        builder = BlazeServerBuilder[IO].bindHttp(config.port, config.host)
        frontendJS = config.mode + ".js"
        routes = new Routes(blocker, frontendJS).routes

        app = GZip(routes)

        _ <- builder.withHttpApp(app.orNotFound).resource
      } yield ()

    ServerConfig.apply.parse(args) match {
      case Left(help) =>
        IO.delay(println(help)).as(ExitCode.Error)
      case Right(config) =>
        val status = IO.delay(
          println(
            s"Running server on http://${config.host}:${config.port} (mode: ${config.mode})"
          )
        )
        server(config).use(_ => status *> IO.never).as(ExitCode.Success)
    }
  }
}
