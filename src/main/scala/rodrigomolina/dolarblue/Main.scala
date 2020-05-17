package rodrigomolina.dolarblue

import cats.effect._
import org.http4s.server.blaze._
import rodrigomolina.dolarblue.core.entity
import rodrigomolina.dolarblue.core.usecase.CurrencyService
import rodrigomolina.dolarblue.infrastructure.{CurrencyEndpoint, CurrencyRestRepository, DollarSiClient, HttpGateway}


object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val url = "https://www.dolarsi.com/api/api.php?type=valoresprincipales"

    val dollarClient = DollarSiClient[IO](url, HttpGateway(), new entity.Clock())
    val currencyRepository = CurrencyRestRepository[IO](dollarClient)

    implicit val currencyService = CurrencyService[IO](currencyRepository)

    val currencyRoutes = CurrencyEndpoint[IO](currencyService).routes


    BlazeServerBuilder[IO]
      .bindHttp(8085, "localhost")
      .withHttpApp(currencyRoutes)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }

}
