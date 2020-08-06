package example.frontend

import com.raquo.laminar.api.L._
import org.scalajs.dom

object Client {

  def app = {
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
        .composeChanges(_.debounce(250))
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

    div(
      div("Search: ", filterInput),
      div("Prefix only", prefixOnlyCheckbox),
      results
    )

  }

  def main(args: Array[String]): Unit = {

    documentEvents.onDomContentLoaded.foreach { _ =>
      render(dom.document.getElementById("appContainer"), app)
    }(unsafeWindowOwner)
  }
}
