package rodrigomolina.dolarblue

import cats.Monad
import cats.effect.{IO, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import rodrigomolina.dolarblue.core.CurrencyId
import rodrigomolina.dolarblue.core.usecase.CurrencyService
import rodrigomolina.dolarblue.infrastructure.{CurrencyRestRepository, DollarSiClient}

object Main extends App {

  val url = "https://www.dolarsi.com/api/api.php?type=valoresprincipales"

  val dollarClient = DollarSiClient[IO](url)
  val currencyRepository = CurrencyRestRepository[IO](dollarClient)

  implicit val monad = Monad[IO]
  implicit val console = new LiveConsole[IO]()
  implicit val currencyService = CurrencyService[IO](currencyRepository)

  TaglessMain
    .run()
    .unsafeRunSync()

}

trait Console[F[_]] {
  def putStrLn(line: String): F[Unit]

  def getStrLn: F[String]
}

object Console {
  def apply[F[_]](implicit F: Console[F]): Console[F] = F
}

class LiveConsole[F[_] : Sync] extends Console[F] {
  def putStrLn(line: String): F[Unit] =
    Sync[F].pure(println(line))

  def getStrLn: F[String] =
    Sync[F].pure(scala.io.StdIn.readLine())
}

object TaglessMain {
  def run[F[_] : Monad : Console : CurrencyService](): F[Unit] = {

    for {
      response <- CurrencyService[F].getCurrencyExchange(CurrencyId("DOLLAR_STREET"))
      _ <- Console[F].putStrLn(response.toString)
    } yield ()
  }
}


