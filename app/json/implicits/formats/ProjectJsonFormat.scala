package json.implicits.formats

import models.Project
import play.api.libs.json.{Format, Json, Reads, Writes}

object ProjectJsonFormat {
  import DateTimeJsonFormat._

  implicit val projectReads: Reads[Project] = Json.reads[Project]

  implicit val projectWrites: Writes[Project] = Json.writes[Project]

  implicit val projectFormat: Format[Project] = Format(projectReads, projectWrites)
}