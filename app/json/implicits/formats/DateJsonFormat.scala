package json.implicits.formats

import java.sql.Date

import helpers.DateHelper
import play.api.libs.json._

object DateJsonFormat {
  val format = DateHelper.pattern
  implicit val dateWrites = Reads[Date](js => JsSuccess(DateHelper.sqlDateStringToDate(js.asInstanceOf[JsString].value)))
  implicit val dateReads = Writes[Date](date => JsString(date.toString()))
}
