package example.shared

object Protocol {
  object GetSuggestions {

    case class Request(search: String, prefixOnly: Option[Boolean] = None)
    object Request {
      implicit val codec = io.circe.generic.semiauto.deriveCodec[Request]
    }

    case class Response(suggestions: Seq[String])
    object Response {
      implicit val codec = io.circe.generic.semiauto.deriveCodec[Response]
    }
  }
}
