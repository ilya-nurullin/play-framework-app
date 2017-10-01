package json.implicits.formats

import play.api.libs.json._

object DateTimeJsonFormat {
  val dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
  implicit val dateTimeWrites = JodaWrites.jodaDateWrites(dateTimeFormat)
  implicit val dateTimeReads = JodaReads.jodaDateReads(dateTimeFormat)
}
