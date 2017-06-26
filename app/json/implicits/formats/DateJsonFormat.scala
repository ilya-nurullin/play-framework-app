package json.implicits.formats

import play.api.libs.json._

object DateJsonFormat {
  val format = "yyyy-MM-dd'T'HH:mm:ssZ"
  implicit val dateWrites = Writes.jodaDateWrites(format)
  implicit val dateReads = Reads.jodaDateReads(format)
}
