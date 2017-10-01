package json.implicits.formats

import models.Task
import play.api.libs.json._

object TaskJsonFormat {
  import DateTimeJsonFormat._

  implicit val taskReads: Reads[Task] = Json.reads[Task]

  implicit val taskWrites: Writes[Task] = Json.writes[Task]

  implicit val taskFormat: Format[Task] = Format(taskReads, taskWrites)
}
