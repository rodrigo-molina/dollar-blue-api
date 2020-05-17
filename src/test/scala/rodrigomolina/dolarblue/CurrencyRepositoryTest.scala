package rodrigomolina.dolarblue

import java.time.ZonedDateTime

import cats.effect.IO
import cats.implicits._
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite
import rodrigomolina.dolarblue.core.port.CurrencyRepositoryError
import rodrigomolina.dolarblue.core.entity.{Clock, CurrencyExchange, CurrencyExchangeValue}
import rodrigomolina.dolarblue.infrastructure.CurrencyRestRepository._
import rodrigomolina.dolarblue.infrastructure.{CurrencyRestRepository, DollarSiClient, Gateway}

import scala.io.Source.fromInputStream


class CurrencyRepositoryTest extends AnyFunSuite with MockitoSugar {

  val clock = mock[Clock]
  val gateway = mock[Gateway]
  val DollarSiUrl = "http://localhost/mock"

  val dollarClient = new DollarSiClient[IO](DollarSiUrl, gateway, clock)
  val restRepository = CurrencyRestRepository(dollarClient)

  test("it should get dollar blue value") {
    when(gateway.fromUrl(any[String])) thenReturn resourceAsString("/json/response/dollarsi_response.json")
    when(clock.time()) thenReturn fromString("2020-05-09T21:33:05.878Z")

    val result = restRepository.getCurrencyExchange(Dollar.id, ArgentinePeso.id)
      .unsafeRunSync()

    assert(result ==
      CurrencyExchange(
        from = ArgentinePeso,
        to = Dollar,
        official = CurrencyExchangeValue(64.63, 69.63),
        blue = CurrencyExchangeValue(112.0, 122.0).some,
        queryDate = fromString("2020-05-09T21:33:05.878Z")).asRight[CurrencyRepositoryError])
  }


  def resourceAsString(resourcePath: String) = {
    val stream = getClass.getResourceAsStream(resourcePath)
    fromInputStream(stream).getLines.mkString
  }

  def fromString(input: String): ZonedDateTime = {
    import java.time.ZonedDateTime
    import java.time.format.DateTimeFormatter
    val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    ZonedDateTime.parse(input, dateTimeFormatter)
  }
}
