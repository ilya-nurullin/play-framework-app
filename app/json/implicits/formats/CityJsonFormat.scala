package json.implicits.formats

import models.City
import play.api.libs.json.{Json, Writes}

object CityJsonFormat {
  implicit val cityWrites: Writes[City] = Json.writes[City]
}
