package models

import javax.inject._

import org.joda.time._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.Tag
import slick.jdbc.MySQLProfile.api._
import com.github.tototoshi.slick.MySQLJodaSupport._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random


case class UsersApiToken(token: String, userId: Int, appId: Int, firebaseToken: String, expiresAt: DateTime)

class UsersApiTokenTable(tag: Tag) extends Table[UsersApiToken](tag, "user_has_api_token") {
  def token = column[String]("token", O.PrimaryKey)
  def userId = column[Int]("user_id")
  def appId = column[Int]("app_id")
  def firebaseToken = column[String]("firebase_token")
  def expiresAt = column[DateTime]("expires_at")

  def * = (token, userId, appId, firebaseToken, expiresAt) <> (UsersApiToken.tupled, UsersApiToken.unapply)
}

@Singleton
class UsersApiTokenDAO @Inject()(dbConfigProvider: DatabaseConfigProvider, config: play.api.Configuration)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val usersToken: TableQuery[UsersApiTokenTable] = TableQuery[UsersApiTokenTable]
  import dbConfig.profile.api._

  def getToken(token: String): Future[Option[UsersApiToken]] = dbConfig.db.run {
    usersToken.filter(t => t.token === token && t.expiresAt >= DateTime.now()).take(1).result.headOption
  }

  def updateToken(token: String) = dbConfig.db.run {
    usersToken.filter(t => t.token === token && t.expiresAt >= DateTime.now()).map(_.expiresAt).update(
      DateTime.now().plusDays(config.underlying.getInt("app.models.UsersApiToken.token.lifetime"))
    )
  }

  def removeOldUserAppTokens(appId: Int, userId: Int) = dbConfig.db.run {
    usersToken.filter(u => u.appId === appId && u.userId === userId).delete
  }

  def removeUserAppToken(appId: Int, userId: Int, token: String) = dbConfig.db.run {
    usersToken.filter(r => r.appId === appId && r.userId === userId && r.token === token).delete
  }


  def generateToken(appId: Int, userId: Int, firebaseToken: String, round: Int = 1): Future[(String, DateTime)] = {
    if (round == 11) return Future.failed(new InterruptedException("Too many rounds for generate a token. Maybe primary or unique keys duplications"))

    val tokenLength = Random.alphanumeric.take(config.underlying.getInt("app.models.UsersApiToken.token.length")).mkString
    val expiresAt = DateTime.now().plusDays(config.underlying.getInt("app.models.UsersApiToken.token.lifetime"))

    val query = dbConfig.db.run {
      usersToken += UsersApiToken(tokenLength, userId, appId, firebaseToken, expiresAt)
    }

    query.map(_ => (tokenLength, expiresAt)).recoverWith {
      case e: java.sql.SQLIntegrityConstraintViolationException => generateToken(appId, userId, firebaseToken, round + 1)
    }

  }
}