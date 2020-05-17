package rodrigomolina.dolarblue.infrastructure

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import rodrigomolina.dolarblue.core.entity.CurrencyId
import rodrigomolina.dolarblue.core.port.{ConnectionError, CurrencyNotFoundError}
import rodrigomolina.dolarblue.core.usecase.CurrencyService

case class CurrencyEndpoint[F[_] : Sync](currencyService: CurrencyService[F]) extends Http4sDsl[F] {

  val routes = HttpRoutes.of[F] {
    case request@GET -> Root / "currencies" / currentId / "to" / toId =>
      for {
        exchangeResponse <- currencyService.getCurrencyExchange(CurrencyId(currentId), CurrencyId(toId))
        response <- exchangeResponse match {
          case Right(r) => Ok(r)
          case Left(_: CurrencyNotFoundError) => NotFound("Currency not found.")
          case Left(_: ConnectionError) => FailedDependency("Could not get information from external provider.")
        }
      } yield response
  }.orNotFound
}

