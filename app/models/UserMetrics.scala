package models

import javax.inject._

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.Tag
import slick.jdbc.MySQLProfile.api._

case class UserMetrics(userId: Int, suretyCardsCount: Int, points: Int)

class UserMetricsTable(tag: Tag) extends Table[UserMetrics](tag, "users_metrics") {
  def userId = column[Int]("user_id")
  def suretyCardsCount = column[Int]("surety_cards")
  def points = column[Int]("points")

  def * = (userId, suretyCardsCount, points).mapTo[UserMetrics]
}

@Singleton
class UserMetricsDAO @Inject() (dbConfigProvider: DatabaseConfigProvider) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  val userMetrics = TableQuery[UserMetricsTable]

  def suretyCards_++(userId: Int) = dbConfig.db.run {
    sqlu"UPDATE ${userMetrics.baseTableRow.tableName} SET surety_cards = surety_cards + 1"
  }

  def setSuretyCardsCount(userId: Int, count: Int) = dbConfig.db.run {
    userMetrics.filter(_.userId === userId).map(_.suretyCardsCount).update(count)
  }

  def points_+(userId: Int, count: Int) = dbConfig.db.run {
    sqlu"UPDATE ${userMetrics.baseTableRow.tableName} SET points = points + ${count}"
  }
}