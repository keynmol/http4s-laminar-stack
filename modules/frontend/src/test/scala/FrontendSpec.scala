package example.frontend

import scala.collection.mutable
import scala.concurrent.Future

import com.raquo.domtestutils.EventSimulator
import com.raquo.domtestutils.matching.RuleImplicits
import com.raquo.laminar.api.L._
import example.shared.Protocol
import org.scalajs.dom
import org.scalajs.dom.raw.Event
import org.scalajs.dom.raw.EventInit
import utest._

object ClientSpect extends TestSuite with EventSimulator with RuleImplicits {
  val tests = Tests {
    test("client respects `prefix only` checkbox") {
      val calls = mutable.ListBuffer.empty[(String, Boolean)]

      val t = testApi {
        case (s, b) =>
          calls.addOne(s -> b)
          Right(List("hello" + s, "world" + b.toString()))
      }

      harness(t) { testApp =>
        testApp.prefixFilter.click()
        testApp.prefixFilter.click()

        assert(calls.toList == List("" -> false, "" -> true, "" -> false))
      }
    }

    test("respects `search` input") {
      val calls = mutable.ListBuffer.empty[(String, Boolean)]

      val t = testApi {
        case (s, b) =>
          calls.addOne(s -> b)
          Right(List("hello" + s, "world" + b.toString()))
      }

      harness(t) { testApp =>
        testApp.searchBox.value = "bla"

        testApp.searchBox.dispatchEvent(new Event("input", new EventInit {
          bubbles = true
        }))

        assert(calls.toList == List("" -> false, "bla" -> false))
      }
    }

  }

  case class TestApp(
      prefixFilter: dom.html.Input,
      searchBox: dom.html.Input,
      results: dom.html.Element
  )

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
