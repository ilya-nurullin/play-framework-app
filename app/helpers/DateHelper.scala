package helpers

import java.sql.Date
import java.text.SimpleDateFormat

object DateHelper {
  def sqlDateStringToDate(date: String): Date = {
    new Date(new SimpleDateFormat("yyyy-MM-dd").parse(date).getTime)
  }
}
