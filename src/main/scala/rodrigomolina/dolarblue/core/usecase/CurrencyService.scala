package rodrigomolina.dolarblue.core.usecase

import cats.Monad
import rodrigomolina.dolarblue.core.entity.{CurrencyExchange, CurrencyId}
import rodrigomolina.dolarblue.core.port.{CurrencyRepository, CurrencyRepositoryError}

case class CurrencyService[F[_]: Monad](currencyRepository: CurrencyRepository[F]) {

  def getCurrencyExchange(from: CurrencyId, to: CurrencyId): F[Either[CurrencyRepositoryError, CurrencyExchange]] = currencyRepository.getCurrencyExchange(from, to)
}

object CurrencyService {
  def apply[F[_]](implicit F: CurrencyService[F]): CurrencyService[F] = F
}