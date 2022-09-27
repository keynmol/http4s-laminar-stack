package example.backend

import cats.effect.IO

import example.shared.Protocol.GetSuggestions

trait Service:
  def getSuggestions(
      request: GetSuggestions.Request
  ): IO[GetSuggestions.Response]

object ServiceImpl extends Service:
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
  ): IO[GetSuggestions.Response] =
    import GetSuggestions.*

    request match
      case Request(search, Some(false) | None) =>
        IO.pure(Response(things.filter(_.contains(search))))
      case Request(search, Some(true)) =>
        IO.pure(Response(things.filter(_.startsWith(search))))
end ServiceImpl
