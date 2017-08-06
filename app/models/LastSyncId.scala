package models

import javax.inject._

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag

import scala.concurrent.ExecutionContext

case class LastSyncId(userId: Int, appId: Int, lastSyncId: Long)

class UserHasLastSyncIdTable(tag: Tag) extends Table[LastSyncId](tag, "user_has_last_sync_id") {
  def userId = column[Int]("user_id")
  def appId = column[Int]("app_id")
  def lastSyncId = column[Long]("last_sync_id")

  def * = (userId, appId, lastSyncId) <> (LastSyncId.tupled, LastSyncId.unapply)
}

@Singleton
class LastSyncIdDAO @Inject() (dbConfigProvider: DatabaseConfigProvider, config: play.api.Configuration)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val lastSyncTable = TableQuery[UserHasLastSyncIdTable]
  import dbConfig.profile.api._

  def getLastSyncId(userId: Int, appId: Int) = dbConfig.db.run {
    lastSyncTable.filter(r => r.userId === userId && r.appId === appId).map(_.lastSyncId).result.headOption
  }

  def updateLastSyncId(userId: Int, appId: Int, newSyncId: Long) = dbConfig.db.run {
    lastSyncTable.filter(r => r.userId === userId && r.appId === appId).map(_.lastSyncId).update(newSyncId)
  }

  def createNewLastSyncId(userId: Int, appId: Int, lastSyncId: Long) = dbConfig.db.run {
    lastSyncTable += LastSyncId(userId, appId, lastSyncId)
  }
}
