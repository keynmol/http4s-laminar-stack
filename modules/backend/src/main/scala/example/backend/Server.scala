package example.backend

import cats.effect._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.GZip

object Server extends IOApp {
  def resource(service: Service, config: ServerConfig) =
    for {
      blocker <- Blocker.apply[IO]

      builder    = BlazeServerBuilder[IO].bindHttp(config.port, config.host)
      frontendJS = config.mode + ".js"
      routes     = new Routes(service, blocker, frontendJS).routes

      app = GZip(routes)

      _ <- builder.withHttpApp(app.orNotFound).resource
    } yield ()

  def run(args: List[String]): IO[ExitCode] = {

    ServerConfig.apply.parse(args) match {
      case Left(help) =>
        IO.delay(println(help)).as(ExitCode.Error)
      case Right(config) =>
        val status = IO.delay(
          println(
            s"Running server on http://${config.host}:${config.port} (mode: ${config.mode})"
          )
        )

        resource(ServiceImpl, config)
          .use(_ => status *> IO.never)
          .as(ExitCode.Success)
    }
  }
}
