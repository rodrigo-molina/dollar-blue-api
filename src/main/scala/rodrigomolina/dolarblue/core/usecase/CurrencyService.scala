package rodrigomolina.dolarblue.core.usecase

import rodrigomolina.dolarblue.core.port.{CurrencyRepository, CurrencyRepositoryError}
import rodrigomolina.dolarblue.core.{CurrencyExchange, CurrencyId}

case class CurrencyService[F[_]](currencyRepository: CurrencyRepository[F]) {

  def getCurrencyExchange(from: CurrencyId, to: CurrencyId): F[Either[CurrencyRepositoryError, CurrencyExchange]] = currencyRepository.getCurrencyExchange(from, to)
}

object CurrencyService {
  def apply[F[_]](implicit F: CurrencyService[F]): CurrencyService[F] = F
}