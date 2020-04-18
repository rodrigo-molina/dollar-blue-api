package rodrigomolina.dolarblue.core

import java.time.ZonedDateTime

case class CurrencyId(id: String)

case class Currency(id: CurrencyId, name: String)

case class CurrencyExchange(from: Currency,
                            to: Currency,
                            buyValue: Float,
                            sellValue: Float,
                            queryDate: ZonedDateTime)