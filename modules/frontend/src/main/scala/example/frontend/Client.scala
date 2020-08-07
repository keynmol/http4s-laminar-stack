package example.frontend

import com.raquo.airstream.signal.Signal
import com.raquo.laminar.api.L._
import org.scalajs.dom

object Client {

  def app(api: Api, debounce: Int = 250) = {
    val searchString = Var("")
    val prefixOnly = Var(false)

    val filterInput = input(
      `type` := "text",
      idAttr := "search-filter",
      inContext(thisNode =>
        onInput.mapTo(thisNode.ref.value) --> searchString.writer
      )
    )

    val prefixOnlyCheckbox = input(
      `type` := "checkbox",
      idAttr := "prefix-only-filter",
      inContext(thisNode =>
        onChange.mapTo(thisNode.ref.checked) --> prefixOnly.writer
      ),
      inContext(_ =>
        onScroll
          .mapTo({ println("hello"); prefixOnly.now() }) --> prefixOnly.writer
      )
    )

    val debounced =
      if (debounce > 0)
        searchString.signal
          .combineWith(prefixOnly.signal)
          .composeChanges(_.debounce(debounce))
      else searchString.signal.combineWith(prefixOnly.signal)

    val resolved =
      debounced
        .flatMap(r => Signal.fromFuture(api.post(r._1, r._2)))
        .map {
          case None => img(src := "/assets/ajax-loader.gif")
          case Some(Right(response)) =>
            ul(
              response.suggestions.map(sug => li(sug))
            )
          case Some(Left(err)) => b(err.toString)
        }

    val results =
      div(idAttr := "results", child <-- resolved)

    div(
      div("Search: ", filterInput),
      div("Prefix only", prefixOnlyCheckbox),
      results
    )

  }

  def main(args: Array[String]): Unit = {

    documentEvents.onDomContentLoaded.foreach { _ =>
      render(dom.document.getElementById("appContainer"), app(FutureApi))
    }(unsafeWindowOwner)
  }
}
