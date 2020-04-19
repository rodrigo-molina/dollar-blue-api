package rodrigomolina.dolarblue.core.port

import cats.effect.IO
import rodrigomolina.dolarblue.core.{CurrencyExchange, CurrencyId}

trait CurrencyRepository {

  def getCurrencyExchange(id: CurrencyId): IO[Either[CurrencyRepositoryError, CurrencyExchange]]

}

class CurrencyRepositoryError() extends Error
case class CurrencyNotFoundError() extends CurrencyRepositoryError
case class ConnectionError() extends CurrencyRepositoryError
