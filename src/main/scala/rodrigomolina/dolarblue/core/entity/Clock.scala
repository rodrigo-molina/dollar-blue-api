package rodrigomolina.dolarblue.core.entity

import java.time.{ZoneOffset, ZonedDateTime}

class Clock {
  def time() = ZonedDateTime.now(ZoneOffset.UTC)
}