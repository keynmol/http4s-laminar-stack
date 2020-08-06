package example.frontend

import com.raquo.laminar.api.L._
import utest._
import com.raquo.laminar.nodes.ChildNode

object HelloTests extends TestSuite {
  val tests = Tests {
    test("hello") {
      val document = org.scalajs.dom.document

      val el = document.createElement("div")

      document.body.appendChild(el)
      println(ChildNode.isNodeMounted(el))

      render(el, Client.app)

      println(el.innerHTML)

      assert(1 == 1)
    }
  }
}

object LaminarApp {
  import com.raquo.laminar.api.L._

  val app = div("hello")
}
