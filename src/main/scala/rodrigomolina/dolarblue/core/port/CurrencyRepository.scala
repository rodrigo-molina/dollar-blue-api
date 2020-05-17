package rodrigomolina.dolarblue.core.port

import cats.Monad
import rodrigomolina.dolarblue.core.entity.{CurrencyExchange, CurrencyId}

trait CurrencyRepository[F[_]] {

  def getCurrencyExchange(from: CurrencyId, to: CurrencyId): F[Either[CurrencyRepositoryError, CurrencyExchange]]

}

class CurrencyRepositoryError() extends Error
case class CurrencyNotFoundError() extends CurrencyRepositoryError
case class ConnectionError() extends CurrencyRepositoryError
