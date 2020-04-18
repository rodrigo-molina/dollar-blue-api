package rodrigomolina.dolarblue

import java.time.{ZoneOffset, ZonedDateTime}

import cats.effect.IO

import scala.io.Source.fromURL

object Main extends App {

  val url = "https://www.dolarsi.com/api/api.php?type=valoresprincipales"
  val client = DollarSiClient(url)

  val printValue: String => IO[Unit] = (value: String) => IO {
    println(s"This is the response: $value")
  }

  val program: IO[Unit] =
    for {
      response <- client.getDollarValue()
      _ <- printValue(response.toString)
    } yield ()

  program.unsafeRunSync()

}


case class Currency(name: String, code: String)

case class CurrencyExchange(
                             from: Currency,
                             to: Currency,
                             buyValue: Float,
                             sellValue: Float,
                             queryDate: ZonedDateTime
                           )


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
          Currency("Peso Argentino", "PESO_AR"),
          Currency("Dollar Blue", "DOLLAR_STREET"),
          response.compra.replace(",", ".").toFloat,
          response.venta.replace(",", ".").toFloat,
          ZonedDateTime.now(ZoneOffset.UTC))
      )
      .head
  }


}
