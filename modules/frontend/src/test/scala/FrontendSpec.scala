package example.frontend

import scala.collection.mutable
import scala.concurrent.Future

import com.raquo.laminar.api.L._
import example.shared.Protocol
import org.scalajs.dom
import org.scalajs.dom.raw.Event
import org.scalajs.dom.raw.EventInit
import utest._

object ClientSpect extends TestSuite {
  val tests = Tests {
    Symbol("whole client tests") {
      test("client respects `prefix only` checkbox") {
        val calls = mutable.ListBuffer.empty[(String, Boolean)]

        val t = testApi { case (s, b) =>
          calls.addOne(s -> b)
          Right(List("hello", "world"))
        }

        harness(t) { testApp =>
          testApp.prefixFilter.click()
          testApp.prefixFilter.click()

          assert(calls.toList == List("" -> false, "" -> true, "" -> false))
        }
      }

      test("respects `search` input") {
        val calls = mutable.ListBuffer.empty[(String, Boolean)]

        val t = testApi { case (s, b) =>
          calls.addOne(s -> b)
          Right(List("hello", "world"))
        }

        harness(t) { testApp =>
          testApp.simulateValueInput(testApp.searchBox, "bla")

          assert(calls.toList == List("" -> false, "bla" -> false))

          testApp.simulateValueInput(testApp.searchBox, "something")

          assert(
            calls.toList == List(
              ""          -> false,
              "bla"       -> false,
              "something" -> false
            )
          )
        }
      }

      test("renders the results correctly") {
        val apiReturn = List("a", "b", "c", "d")

        val t = testApi { case (s, _) =>
          if (s == "test")
            Right(apiReturn)
          else Right(Nil)
        }

        harness(t) { testApp =>
          def renderedResults = testApp.results.getElementsByTagName("li")

          assert(renderedResults.length == 0)

          testApp.simulateValueInput(testApp.searchBox, "test")

          assert(renderedResults.length == apiReturn.length)

          apiReturn.zipWithIndex.foreach { case (expected, idx) =>
            assert(renderedResults.apply(idx).innerHTML == expected)
          }
        }

      }
    }

  }

  case class TestApp(
      prefixFilter: dom.html.Input,
      searchBox: dom.html.Input,
      results: dom.html.Element
  ) {
    def simulateValueInput(inp: dom.html.Input, value: String) = {
      inp.value = value
      inp.dispatchEvent(
        new Event(
          "input",
          new EventInit {
            bubbles = true
          }
        )
      )
    }
  }

  def harness(testApi: Api)(f: TestApp => Unit) = {
    import dom.document

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

    try { f(TestApp(prefixFilter, searchBox, results)) }
    finally {
      root.unmount()
    }
  }

  def testApi(f: (String, Boolean) => Either[Throwable, List[String]]) =
    new Api {
      import Protocol.GetSuggestions.Response

      override def post(
          search: String,
          prefixOnly: Boolean
      ): Future[Either[Throwable, Response]] =
        Future.successful(f(search, prefixOnly).map(Response.apply))
    }
}
