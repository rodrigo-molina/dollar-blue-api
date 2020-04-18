package rodrigomolina.dolarblue

import cats.effect.IO
import rodrigomolina.dolarblue.Configuration._
import rodrigomolina.dolarblue.core.CurrencyId
import rodrigomolina.dolarblue.core.usecase.CurrencyService
import rodrigomolina.dolarblue.infrastructure.{CurrencyRestRepository, DollarSiClient}

object Main extends App {

  val printValue: String => IO[Unit] = (value: String) => IO {
    println(s"This is the response: $value")
  }

  val program: IO[Unit] =
    for {
      response <- currencyService.getCurrencyExchange(CurrencyId("DOLLAR_STREET"))
      _ <- printValue(response.toString)
    } yield ()

  program.unsafeRunSync()

}

object Configuration {
  val url = "https://www.dolarsi.com/api/api.php?type=valoresprincipales"
  val dollarClient = DollarSiClient(url)
  val currencyRepository = CurrencyRestRepository(dollarClient)
  val currencyService = CurrencyService(currencyRepository)
}


