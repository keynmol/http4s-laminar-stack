package example.shared

import io.circe.{Decoder, Encoder}

object Protocol:
  object GetSuggestions:

    case class Request(
        search: String,
        prefixOnly: Option[Boolean] = None
    ) derives Decoder,
          Encoder.AsObject

    case class Response(suggestions: Seq[String])
        derives Decoder,
          Encoder.AsObject
