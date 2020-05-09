package rodrigomolina.dolarblue.infrastructure

import scala.io.Source.fromURL

trait Gateway {
  def fromUrl(url: String): String
}

case class HttpGateway() extends Gateway {
  def fromUrl(url: String): String = fromURL(url).mkString
}
