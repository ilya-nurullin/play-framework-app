package models

import java.sql.Date
import javax.inject._

import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{TableQuery, Tag}
import com.github.tototoshi.slick.MySQLJodaSupport._

import scala.concurrent.Future
import scala.util.Random


case class User(id: Int, login: String, email: String, passHash: String, name: Option[String] = None,
                avatar: Option[String] = None, aboutMyself: Option[String] = None, dateOfBirth: Option[Date] = None,
                sex: Option[String] = None, createdAt: DateTime, updatedAt: DateTime,
                cityId: Option[Int] = None, statuses: Option[String] = None, userRankId: Int,
                premiumUntil: Option[DateTime] = None, isBanned: Boolean = false)

class UsersTable(tag: Tag) extends Table[User](tag, "users") {

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def login = column[String]("login")
  def email = column[String]("email")
  def passHash = column[String]("pass_hash")
  def name = column[Option[String]]("name")
  def avatar = column[Option[String]]("avatar")
  def aboutMyself = column[Option[String]]("about_myself")
  def dateOfBirth = column[Option[Date]]("date_of_birth")
  def sex = column[Option[String]]("sex")
  def createdAt = column[DateTime]("created_at")
  def updatedAt = column[DateTime]("updated_at")
  def cityId = column[Option[Int]]("city_id")
  def statuses = column[Option[String]]("statuses")
  def userRankId = column[Int]("user_rank_id")
  def premiumUntil = column[Option[DateTime]]("premium_until")
  def isBanned = column[Boolean]("is_banned")

  def * = (id, login, email, passHash, name, avatar, aboutMyself, dateOfBirth, sex, createdAt, updatedAt, cityId, statuses,
    userRankId, premiumUntil, isBanned) <> ( User.tupled, User.unapply )
}

@Singleton
class UserDAO @Inject()(dbConfigProvider: DatabaseConfigProvider) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig.profile.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  def getById(id: Int): Future[Option[User]] = {
    dbConfig.db.run(UserQueries.getById(id).result.headOption)
  }

  def getByEmail(email: String): Future[Option[User]] = {
    dbConfig.db.run(UserQueries.getByEmail(email).result.headOption)
  }

  def create(email: String, passHash: String): Future[Int] = {
    val login = Random.alphanumeric.take(20).mkString
    dbConfig.db.run {
      (UserQueries.users returning UserQueries.users.map(_.id)) += User(0, login, email, passHash, userRankId = 1,
        createdAt = DateTime.now(), updatedAt = DateTime.now())
    }.map { id =>
      dbConfig.db.run {
        UserQueries.users.filter(_.login === login).map(_.login).update("id"+id)
      }
      id
    }
  }

  def update() = ???

}

object UserQueries {
  val users: TableQuery[UsersTable] = TableQuery[UsersTable]

  def getById(id: Int) = users.filter(_.id === id).take(1)

  def getByEmail(email: String) = users.filter(_.email === email).take(1)

}
