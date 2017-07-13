package json.implicits

import play.api.libs.json.{JsArray, Json}
import slick.jdbc.MySQLProfile.api._

trait JsArrayMappedColumn {
  implicit val jsonArrayColumnType = MappedColumnType.base[JsArray, String](_.toString, Json.parse(_).asInstanceOf[JsArray])
}
