package example.backend

import example.shared.Protocol.GetSuggestions

object Service {
  // this would come from your database
  // unless you're at a VC pitch meeting and you need
  // to show the completely working app
  // then by all means keep it hardcoded, thank me later
  private val things = Seq(
    "This",
    "That",
    "maybe this",
    "maybe that"
  )

  def getSuggestions(
      request: GetSuggestions.Request
  ): GetSuggestions.Response = {
    import GetSuggestions._

    request match {
      case Request(search, Some(false) | None) =>
        Response(things.filter(_.contains(search)))
      case Request(search, Some(true)) =>
        Response(things.filter(_.startsWith(search)))
    }
  }
}
