package models.surety

import javax.inject._

import com.github.tototoshi.slick.MySQLJodaSupport._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{TableQuery, Tag}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

case class Whip(taskId: Long, guarantorId: Int, message: Option[String], image: Option[String], whippedAt: DateTime = DateTime.now())

class WhipTable(tag: Tag) extends Table[Whip](tag, "whips") {
  def taskId = column[Long]("task_id")
  def guarantorId = column[Int]("guarantor_id")
  def message = column[Option[String]]("message")
  def image = column[Option[String]]("image")
  def whippedAt = column[DateTime]("whipped_at")

  def * = (taskId, guarantorId, message, image, whippedAt).mapTo[Whip]
}

@Singleton
class WhipDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  val whips = TableQuery[WhipTable]

  def whipTo(taskId: Long, guarantorId: Int, message: Option[String], image: Option[String]) = dbConfig.db.run {
    whips += Whip(taskId, guarantorId, message, image)
  }
}
