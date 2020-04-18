package rodrigomolina.dolarblue.core.port

import cats.effect.IO
import rodrigomolina.dolarblue.core.{CurrencyExchange, CurrencyId}

trait CurrencyRepository {

  def getCurrencyExchange(id: CurrencyId): IO[Either[Error, CurrencyExchange]]

}

case class CurrencyNotFoundError() extends Error
