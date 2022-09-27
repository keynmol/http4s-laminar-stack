package example.frontend

import scala.concurrent.Future

import cats.effect.IO
import cats.effect.Resource

import com.raquo.laminar.api.L.*
import example.shared.Protocol.GetSuggestions.Response
import org.scalajs.dom
import org.scalajs.dom.raw.Event
import org.scalajs.dom.raw.EventInit

trait Harness:
  case class TestApp(
      prefixFilter: dom.html.Input,
      searchBox: dom.html.Input,
      results: dom.html.Element
  ):
    def simulateValueInput(inp: dom.html.Input, value: String) =
      inp.value = value
      inp.dispatchEvent(
        new Event(
          "input",
          new EventInit:
            bubbles = true
        )
      )

  def harness(testApi: Api): Resource[IO, TestApp] =
    import dom.document

    val acquire = IO {

      val el = document.createElement("div")

      document.body.appendChild(el)

      val root = render(el, Client.app(testApi, 0))

      val prefixFilter = document
        .getElementById("prefix-only-filter")
        .asInstanceOf[dom.html.Input]

      val searchBox =
        document.getElementById("search-filter").asInstanceOf[dom.html.Input]

      val results =
        document.getElementById("results").asInstanceOf[dom.html.Element]

      TestApp(prefixFilter, searchBox, results) -> root
    }

    Resource
      .make(acquire) { case (_, node) => IO(node.unmount()).void }
      .map(_._1)
  end harness

  def testApi(f: (String, Boolean) => Either[Throwable, List[String]]) =
    new Api:
      override def post(
          search: String,
          prefixOnly: Boolean
      ): Future[Either[Throwable, Response]] =
        Future.successful(f(search, prefixOnly).map(Response.apply))
end Harness
