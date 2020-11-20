package example.backend

import scala.io.Source

import cats.effect.Blocker
import cats.effect.ContextShift
import cats.effect.IO
import cats.effect.Resource
import cats.effect.Timer

import org.http4s.Method
import org.http4s.Request
import org.http4s.Response
import org.http4s.Uri
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl._
import org.http4s.implicits._

import _root_.io.circe.syntax._
import example.shared.Protocol

object RoutesSpec extends weaver.IOSuite with Http4sDsl[IO] {
  override type Res = Probe
  override def sharedResource: Resource[IO, Res] =
    Blocker[IO].map(Probe(_))

  test("serves frontend from specified resource file") { probe =>
    probe
      .copy(frontendFile = "frontend.js")
      .get(uri"/frontend/app.js")
      .zipWithBody
      .map { case (response, responseBody) =>
        expect.all(
          response.status.code == 200,
          responseBody == probe.read("frontend.js")
        )
      }
  }

  test("serves assets with allowed extensions") { probe =>
    probe
      .get(uri"/assets/allowed.css")
      .zipWithBody
      .map { case (response, responseBody) =>
        expect.all(
          response.status.code == 200,
          responseBody == probe.read("assets/allowed.css")
        )
      }
  }

  test("returns 404 for assets with with disallowed extensions") { probe =>
    probe
      .get(uri"/assets/secret.password")
      .map { response =>
        expect.all(
          response.status.code == 404
        )
      }
  }

  test("calls the service on /get-suggestions") { probe =>
    import Protocol.{GetSuggestions => GS}

    val stubResponse = GS.Response(
      Seq("a", "b", "c", "d")
    )

    val serviceImpl = new Service {
      override def getSuggestions(
          request: GS.Request
      ): IO[GS.Response] = IO(stubResponse)
    }

    val request = GS.Request("hello!")

    probe
      .copy(serviceImpl = serviceImpl)
      .routes()
      .run(
        Request(
          POST,
          uri"/get-suggestions"
        ).withEntity(request)
      )
      .zipWithBody
      .map { case (response, responseBody) =>
        expect.all(
          response.status.code == 200,
          responseBody == stubResponse.asJson.noSpaces
        )
      }
  }

  implicit class RespOps(resp: IO[Response[IO]]) {
    def readBody: IO[String] =
      resp.flatMap(_.bodyAsText.compile.toVector.map(_.mkString))

    def zipWithBody: IO[(Response[IO], String)] =
      resp.parProduct(readBody)
  }

}

case class Probe(
    blocker: Blocker,
    serviceImpl: Service = ServiceImpl,
    frontendFile: String = "test-file"
)(implicit
    timer: Timer[IO],
    cs: ContextShift[IO]
) {

  val classloader = getClass().getClassLoader()

  def read(path: String) =
    Source
      .fromResource(path, classloader)
      .mkString

  def get(uri: Uri) = routes().run(
    Request(
      Method.GET,
      uri
    )
  )

  def routes() =
    new Routes(serviceImpl, blocker, frontendFile).routes.orNotFound

  def routes(frontendJs: String) =
    new Routes(serviceImpl, blocker, frontendJs).routes.orNotFound

  def routes(service: Service, frontendJs: String) =
    new Routes(service, blocker, frontendJs).routes.orNotFound
}
