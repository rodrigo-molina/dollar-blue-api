package rodrigomolina.dolarblue

import cats.Monad
import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import io.circe.generic.auto._
import org.http4s.server.blaze._
import org.http4s.circe.CirceEntityEncoder._
import rodrigomolina.dolarblue.core.port.{ConnectionError, CurrencyNotFoundError}
import rodrigomolina.dolarblue.core.{Clock, CurrencyId}
import rodrigomolina.dolarblue.core.usecase.CurrencyService
import rodrigomolina.dolarblue.infrastructure.{CurrencyRestRepository, DollarSiClient, HttpGateway}


object RestMain extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val url = "https://www.dolarsi.com/api/api.php?type=valoresprincipales"

    val dollarClient = DollarSiClient[IO](url, HttpGateway(), new Clock())
    val currencyRepository = CurrencyRestRepository[IO](dollarClient)

    implicit val monad = Monad[IO]
    implicit val console = new LiveConsole[IO]()
    implicit val currencyService = CurrencyService[IO](currencyRepository)


    val currencyRoutes = HttpRoutes.of[IO] {
      case GET -> Root / "currencies" /  currentId / "to" / toId =>
        currencyService.getCurrencyExchange(CurrencyId(currentId), CurrencyId(toId)).flatMap(_ match {
          case Right(r) => Ok(r)
          case Left(_: CurrencyNotFoundError) => NotFound("Currency not found.")
          case Left(_: ConnectionError) => FailedDependency("Could not get information from external provider.")
        })
    }.orNotFound


    BlazeServerBuilder[IO]
      .bindHttp(8085, "localhost")
      .withHttpApp(currencyRoutes)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }

}
