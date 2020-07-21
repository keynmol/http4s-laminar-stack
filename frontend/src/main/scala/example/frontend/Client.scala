package example.frontend

import com.raquo.laminar.api.L._
import org.scalajs.dom
import sttp.client.FetchBackend
import scala.concurrent.Future
import com.raquo.airstream.signal.Signal

object Client {
  def main(args: Array[String]): Unit = {
    val result = div("Hello, world!!")

    val searchString = Var("")
    val prefixOnly = Var(false)

    val filterInput = input(
      `type` := "text",
      inContext(thisNode =>
        onInput.mapTo(thisNode.ref.value) --> searchString.writer
      )
    )

    val prefixOnlyCheckbox = input(
      `type` := "checkbox",
      inContext(thisNode =>
        onChange.mapTo(thisNode.ref.checked) --> prefixOnly.writer
      )
    )

    val resolved =
      searchString.signal
        .combineWith(prefixOnly.signal)
        .flatMap((Api.post _).tupled)
        .map {
          case None => img(src := "/assets/ajax-loader.gif")
          case Some(Right(response)) =>
            ul(
              response.suggestions.map(sug => li(sug))
            )
          case Some(Left(err)) => b(err.toString)
        }

    val results =
      div(child <-- resolved)

    val app = div(
      div("Search: ", filterInput),
      div("Prefix only", prefixOnlyCheckbox),
      results
    )

    documentEvents.onDomContentLoaded.foreach { _ =>
      render(dom.document.getElementById("appContainer"), app)
    }(unsafeWindowOwner)
  }
}

object Api {
  implicit val backend = FetchBackend()

  import sttp.client._
  import sttp.client.circe._

  import scala.concurrent.ExecutionContext.Implicits.global

  import example.shared.Protocol.GetSuggestions

  def post(search: String, prefixOnly: Boolean = false) = {

    val req = basicRequest
      .post(uri"http://localhost:8080/get-suggestions")
      .body(GetSuggestions.Request(search, Some(prefixOnly)))
      .response(asJson[GetSuggestions.Response])

    val freq = req.send[Future].map(_.body)

    Signal.fromFuture(freq)
  }
}
