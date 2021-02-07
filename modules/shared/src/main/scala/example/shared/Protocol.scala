package example.shared

import zio.json._

object Protocol {
  object GetSuggestions {

    final case class Request(search: String, prefixOnly: Option[Boolean] = None)
    object Request {
      implicit final val codec: JsonCodec[Request] = DeriveJsonCodec.gen
    }

    final case class Response(suggestions: Seq[String])
    object Response {
      implicit final val codec: JsonCodec[Response] = DeriveJsonCodec.gen
    }
  }
}
