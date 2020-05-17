package rodrigomolina.dolarblue.core.entity

import java.time.ZonedDateTime

case class CurrencyId(id: String)

case class Currency(id: CurrencyId, name: String)

case class CurrencyExchange(from: Currency,
                            to: Currency,
                            official: CurrencyExchangeValue,
                            blue: Option[CurrencyExchangeValue],
                            queryDate: ZonedDateTime)

case class CurrencyExchangeValue(buy: Double,
                                 sell: Double)