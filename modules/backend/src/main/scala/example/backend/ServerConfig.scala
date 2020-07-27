package example.backend

import cats.implicits._
import com.monovore.decline._

case class ServerConfig(
    host: String,
    port: Int,
    mode: String
)

object ServerConfig {
  private val DefaultHost = "0.0.0.0"
  private val DefaultPort = 9000
  private val DefaultMode = "dev"

  private val hostOpt = Opts
    .option[String]("host", help = "Host to bind to")
    .withDefault(DefaultHost)
  private val portOpt =
    Opts.option[Int]("port", help = "Port to bind to").withDefault(DefaultPort)

  private val modeOpt = Opts
    .option[String]("mode", help = "Mode (dev or prod)")
    .withDefault("dev")
    .validate("must be one of: dev, prod")(Set("dev", "prod").contains)

  def apply: Command[ServerConfig] =
    Command("", "Run backend and assets server")(
      (hostOpt, portOpt, modeOpt).mapN(ServerConfig.apply)
    )
}
