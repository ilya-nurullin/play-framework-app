package models

import javax.inject._

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.{TableQuery, Tag}
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

case class ApiApp(id: Int, key: String, isBanned: Boolean)

class ApiAppTable(tag: Tag) extends Table[ApiApp](tag, "api_apps") {
  def id = column[Int]("id", O.PrimaryKey)
  def key = column[String]("key", O.Unique)
  def isBanned = column[Boolean]("is_banned")

  def * = (id, key, isBanned) <> (ApiApp.tupled, ApiApp.unapply)
}

@Singleton
class ApiAppDAO @Inject()(dbConfigProvider: DatabaseConfigProvider) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val apiApp: TableQuery[ApiAppTable] = TableQuery[ApiAppTable]
  import dbConfig.profile.api._

  def getApp(key: String) = dbConfig.db.run {
    apiApp.filter(_.key === key).take(1).result.headOption
  }

  def isAppOk(key: String) = getApp(key).map { optApp =>
    optApp.exists { app => !app.isBanned }
  }
}