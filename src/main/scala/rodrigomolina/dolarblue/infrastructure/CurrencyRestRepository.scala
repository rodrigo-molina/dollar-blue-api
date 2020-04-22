package rodrigomolina.dolarblue.infrastructure

import java.time.{ZoneOffset, ZonedDateTime}

import cats.effect.Sync
import cats.implicits._
import rodrigomolina.dolarblue.core.port.{ConnectionError, CurrencyNotFoundError, CurrencyRepository, CurrencyRepositoryError}
import rodrigomolina.dolarblue.core.{Currency, CurrencyExchange, CurrencyId}

import scala.io.Source.fromURL
import scala.util.{Failure, Success, Try}

case class CurrencyRestRepository[F[_] : Sync](dollarClient: DollarSiClient[F]) extends CurrencyRepository[F] {
  val DollarBlueId = "DOLLAR_STREET"

  override def getCurrencyExchange(id: CurrencyId): F[Either[CurrencyRepositoryError, CurrencyExchange]] = id match {
    case CurrencyId(DollarBlueId) => dollarClient.getDollarValue()
    case _ => Sync[F].pure {
      CurrencyNotFoundError().asLeft[CurrencyExchange]
    }
  }

}

case class DollarSiClient[F[_] : Sync](baseUrl: String) {

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
      val dollarSiResponse = fromURL(baseUrl).mkString
        .parseJson.asInstanceOf[JsArray]
        .elements.map(_.convertTo[Casa].casa)

      dollarSiResponse
        .filter(_.nombre.toLowerCase == "dolar blue")
        .map(response =>
          CurrencyExchange(
            Currency(CurrencyId("PESO_AR"), "Peso Argentino"),
            Currency(CurrencyId("DOLLAR_STREET"), "Dollar Blue"),
            response.compra.replace(",", ".").toFloat,
            response.venta.replace(",", ".").toFloat,
            ZonedDateTime.now(ZoneOffset.UTC))
        )
        .head
    } match {
      case Success(value) => value.asRight[CurrencyRepositoryError]
      case Failure(_) => ConnectionError().asLeft[CurrencyExchange]
    }

  }

}
