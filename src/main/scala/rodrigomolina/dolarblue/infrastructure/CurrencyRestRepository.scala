package rodrigomolina.dolarblue.infrastructure

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import rodrigomolina.dolarblue.core.{entity, _}
import rodrigomolina.dolarblue.core.entity.{Clock, Currency, CurrencyExchange, CurrencyExchangeValue, CurrencyId}
import rodrigomolina.dolarblue.core.port.{ConnectionError, CurrencyNotFoundError, CurrencyRepository, CurrencyRepositoryError}
import rodrigomolina.dolarblue.infrastructure.CurrencyRestRepository._

import scala.util.{Failure, Success, Try}

object CurrencyRestRepository {
  val Dollar = Currency(CurrencyId("DOLLAR"), "Dollar")
  val ArgentinePeso = Currency(CurrencyId("PESO_ARGENTINE"), "Peso Argentino")
}

case class CurrencyRestRepository[F[_] : Sync: Monad](dollarClient: DollarSiClient[F]) extends CurrencyRepository[F] {

  override def getCurrencyExchange(from: CurrencyId, to: CurrencyId): F[Either[CurrencyRepositoryError, CurrencyExchange]] = from match {
    case Dollar.id => to match {
      case ArgentinePeso.id => dollarClient.getDollarValue
      case _ => notFound
    }
    case _ => notFound
  }

  private def notFound: F[Either[CurrencyRepositoryError, CurrencyExchange]] = Sync[F].pure {
    CurrencyNotFoundError().asLeft[CurrencyExchange]
  }
}

case class DollarSiClient[F[_] : Sync](baseUrl: String, gateway: Gateway, clock: Clock) {

  import spray.json._
  import DefaultJsonProtocol._


  case class Item(compra: String,
                  venta: String,
                  agencia: String,
                  nombre: String,
                  variacion: Option[String],
                  ventaCero: Option[String],
                  decimales: Option[String])

  case class Casa(casa: Item)

  implicit val dolarFormat = jsonFormat7(Item)
  implicit val casaFormat = jsonFormat1(Casa)


  def getDollarValue(): F[Either[CurrencyRepositoryError, CurrencyExchange]] = Sync[F].pure {
    Try {
      val dollarSiResponse: List[Item] = gateway.fromUrl(baseUrl)
        .parseJson.asInstanceOf[JsArray]
        .elements.map(_.convertTo[Casa].casa)
        .toList

      val responseMap = dollarSiResponse.map(i => (i.nombre.toLowerCase, i)).toMap

      val official: Option[Item] = responseMap.get("dolar oficial")
      val blue: Option[Item] = responseMap.get("dolar blue")

      entity.CurrencyExchange(
        ArgentinePeso,
        Dollar,
        official.map(i => CurrencyExchangeValue(parsetoDouble(i.compra), parsetoDouble(i.venta))).get, // FIXME: avoid .get statement by retuning typed error
        blue.map(i => CurrencyExchangeValue(parsetoDouble(i.compra), parsetoDouble(i.venta))),
        clock.time)

    } match {
      case Success(value) => value.asRight[CurrencyRepositoryError]
      case Failure(_) => ConnectionError().asLeft[CurrencyExchange]
    }

  }

  private def parsetoDouble(input: String): Double = input.replace(",", ".").toDouble


}
