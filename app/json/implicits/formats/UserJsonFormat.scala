package json.implicits.formats

import models.User
import play.api.libs.json._

object UserJsonFormat {
  import DateTimeJsonFormat._

  implicit val userReads: Reads[User] = Json.reads[User]

  implicit val userWrites: Writes[User] = Json.writes[User]

  implicit val userFormat: Format[User] = Format(userReads, userWrites)
}
