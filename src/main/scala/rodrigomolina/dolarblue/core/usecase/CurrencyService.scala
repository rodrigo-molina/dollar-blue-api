package rodrigomolina.dolarblue.core.usecase

import cats.effect.IO
import rodrigomolina.dolarblue.core.port.CurrencyRepository
import rodrigomolina.dolarblue.core.{CurrencyExchange, CurrencyId}

case class CurrencyService(currencyRepository: CurrencyRepository) {

  def getCurrencyExchange(id: CurrencyId): IO[Either[Error, CurrencyExchange]] = currencyRepository.getCurrencyExchange(id)
}
