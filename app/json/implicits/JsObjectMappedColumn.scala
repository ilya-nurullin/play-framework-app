package json.implicits

import play.api.libs.json.{JsObject, Json}
import slick.jdbc.MySQLProfile.api._

trait JsObjectMappedColumn {
  implicit val jsonColumnType = MappedColumnType.base[JsObject, String](_.toString, Json.parse(_).asInstanceOf[JsObject])
}
