package json.implicits.formats

import play.api.libs.json._

object DateJsonFormat {
  val format = "yyyy-MM-dd'T'HH:mm:ssZ"
  implicit val dateWrites = JodaWrites.jodaDateWrites(format)
  implicit val dateReads = JodaReads.jodaDateReads(format)
}
