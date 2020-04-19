package rodrigomolina.dolarblue.infrastructure

import java.time.{ZoneOffset, ZonedDateTime}

import cats.effect.IO
import cats.implicits._
import rodrigomolina.dolarblue.core.port.{ConnectionError, CurrencyNotFoundError, CurrencyRepository, CurrencyRepositoryError}
import rodrigomolina.dolarblue.core.{Currency, CurrencyExchange, CurrencyId}

import scala.io.Source.fromURL
import scala.util.{Failure, Success, Try}

case class CurrencyRestRepository(dollarClient: DollarSiClient) extends CurrencyRepository {
  val DollarBlueId = "DOLLAR_STREET"

  override def getCurrencyExchange(id: CurrencyId): IO[Either[CurrencyRepositoryError, CurrencyExchange]] = id match {
    case CurrencyId(DollarBlueId) => dollarClient.getDollarValue()
    case _ => IO.pure {
      CurrencyNotFoundError().asLeft[CurrencyExchange]
    }
  }

}

case class DollarSiClient(baseUrl: String) {

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


  def getDollarValue(): IO[Either[CurrencyRepositoryError, CurrencyExchange]] = IO {
    Try {
      val dollarSiResponse = fromURL(baseUrl).mkString
        .parseJson.asInstanceOf[JsArray]
        .elements.map(_.convertTo[Casa].casa)

      dollarSiResponse
        .filter(_.nombre == "Dolar Blue")
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
