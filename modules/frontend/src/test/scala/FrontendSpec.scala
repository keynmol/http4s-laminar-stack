package example.frontend

import scala.collection.mutable

import weaver._

/** Don't get me wrong - this is a very gratuitous use of IO
  * and weaver's abilities.
  *
  * But I will still do it, because why not :)
  */
object ClientSpec extends SimpleIOSuite with Harness {

  simpleTest("client respects `prefix only` checkbox") {

    val calls = mutable.ListBuffer.empty[(String, Boolean)]

    val api = testApi { case (s, b) =>
      calls.addOne(s -> b)
      Right(List("hello", "world"))
    }

    harness(api).use { testApp =>
      testApp.prefixFilter.click()
      testApp.prefixFilter.click()

      expect(calls.toList == List("" -> false, "" -> true, "" -> false))
    }
  }

  simpleTest("respects `search` input") {
    val calls = mutable.ListBuffer.empty[(String, Boolean)]

    val api = testApi { case (s, b) =>
      calls.addOne(s -> b)
      Right(List("hello", "world"))
    }

    harness(api).use { testApp =>
      testApp.simulateValueInput(testApp.searchBox, "bla")

      assert(calls.toList == List("" -> false, "bla" -> false))

      testApp.simulateValueInput(testApp.searchBox, "something")

      expect(
        calls.toList == List(
          ""          -> false,
          "bla"       -> false,
          "something" -> false
        )
      )
    }
  }

  simpleTest("renders the results correctly") {
    val ApiData = List("a", "b", "c", "d")

    val api = testApi { case (s, _) =>
      if (s == "test")
        Right(ApiData)
      else Right(Nil)
    }

    harness(api).use { testApp =>
      def renderedResults = testApp.results.getElementsByTagName("li")

      val lengthBeforeInput = renderedResults.length

      testApp.simulateValueInput(testApp.searchBox, "test")

      val rendered =
        ApiData.indices.map(renderedResults.apply(_).innerHTML).toList

      expect(lengthBeforeInput == 0) and
        expect(renderedResults.length == ApiData.length) and
        expect(rendered == ApiData)
    }

  }
}
