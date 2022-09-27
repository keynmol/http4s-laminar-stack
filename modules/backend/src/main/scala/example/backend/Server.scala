package example.backend

import cats.effect.*

import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.GZip

object Server extends IOApp:
  def resource(service: Service, config: ServerConfig) =
    val frontendJS = config.mode + ".js"
    val routes     = new Routes(service, frontendJS).routes

    val app = GZip(routes)

    EmberServerBuilder
      .default[IO]
      .withPort(config.port)
      .withHost(config.host)
      .withHttpApp(app.orNotFound)
      .build

  def run(args: List[String]): IO[ExitCode] =
    ServerConfig.apply.parse(args) match
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
end Server
