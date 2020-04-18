package rodrigomolina.dolarblue.infrastructure

import java.time.{ZoneOffset, ZonedDateTime}

import cats.effect.IO
import rodrigomolina.dolarblue.core.port.{CurrencyNotFoundError, CurrencyRepository}
import rodrigomolina.dolarblue.core.{Currency, CurrencyExchange, CurrencyId}

import scala.io.Source.fromURL

case class CurrencyRestRepository(dollarClient: DollarSiClient) extends CurrencyRepository {

  override def getCurrencyExchange(id: CurrencyId): IO[Either[Error, CurrencyExchange]] = id match {
    case CurrencyId("DOLLAR_STREET") => dollarClient.getDollarValue().flatMap(value => IO.pure(Right(value)))
    case _ => IO.pure(Left(CurrencyNotFoundError()))
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


  def getDollarValue(): IO[CurrencyExchange] = IO {
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
  }


}
