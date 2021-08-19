package example.frontend

import com.raquo.laminar.api.L.*
import org.scalajs.dom

object Client:

  case class SearchBox private (node: Element, signal: Signal[String])

  object SearchBox:
    def create =
      val node = input(
        `type` := "text",
        idAttr := "search-filter"
      )

      val stream =
        node.events(onInput).mapTo(node.ref.value).startWith("")

      new SearchBox(node, stream)

  case class PrefixOnlyCheckbox private (node: Element, signal: Signal[Boolean])

  object PrefixOnlyCheckbox:
    def create =
      val node = input(
        `type` := "checkbox",
        idAttr := "prefix-only-filter"
      )

      val stream =
        node
          .events(onChange)
          .mapTo(node.ref.checked)
          .startWith(node.ref.checked)

      new PrefixOnlyCheckbox(node, stream)
  end PrefixOnlyCheckbox

  def app(api: Api, debounce: Int = 250) =
    val searchBox  = SearchBox.create
    val prefixOnly = PrefixOnlyCheckbox.create

    val debounced =
      if debounce > 0 then
        searchBox.signal
          .combineWith(prefixOnly.signal)
          .composeChanges(_.debounce(debounce))
      else searchBox.signal.combineWith(prefixOnly.signal)

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
      div("Search: ", searchBox.node),
      div("Prefix only", prefixOnly.node),
      results
    )
  end app

  def main(args: Array[String]): Unit =
    documentEvents.onDomContentLoaded.foreach { _ =>
      render(dom.document.getElementById("appContainer"), app(FutureApi))
    }(unsafeWindowOwner)
end Client
