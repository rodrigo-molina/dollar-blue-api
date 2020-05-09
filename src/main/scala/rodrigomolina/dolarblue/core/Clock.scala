package rodrigomolina.dolarblue.core

import java.time.{ZoneOffset, ZonedDateTime}

class Clock {
  def time() = ZonedDateTime.now(ZoneOffset.UTC)
}