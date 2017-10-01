package helpers

import java.sql.Date
import java.text.SimpleDateFormat

object DateHelper {
  val pattern = "yyyy-MM-dd"

  def sqlDateStringToDate(date: String): Date = {
    new Date(new SimpleDateFormat(pattern).parse(date).getTime)
  }
}
