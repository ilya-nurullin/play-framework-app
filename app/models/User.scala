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
                sex: Option[Boolean] = None, createdAt: DateTime, updatedAt: DateTime,
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
  def sex = column[Option[Boolean]]("sex")
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
  import scala.concurrent.ExecutionContext.Implicits.global

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  val users = TableQuery[UsersTable]

  def getById(id: Int): Future[Option[User]] = {
    dbConfig.db.run(users.filter(_.id === id).take(1).result.headOption)
  }

  def getByEmail(email: String): Future[Option[User]] = {
    dbConfig.db.run(users.filter(_.email === email).take(1).result.headOption)
  }

  def create(email: String, passHash: String): Future[Int] = {
    val login = Random.alphanumeric.take(20).mkString
    dbConfig.db.run {
      (users returning users.map(_.id)) += User(0, login, email, passHash, userRankId = 1,
        createdAt = DateTime.now(), updatedAt = DateTime.now())
    }.map { id =>
      dbConfig.db.run {
        users.filter(_.login === login).map(_.login).update("id"+id)
      }
      id
    }
  }

  def update(userId: Int, login: String, email: String, name: Option[String], avatar: Option[String],
             aboutMyself: Option[String], dateOfBirth: Option[Date], sex: Option[Boolean], cityId: Option[Int]) = {
    dbConfig.db.run {
      users.filter(_.id === userId).map(r => (r.login, r.email, r.name, r.avatar, r.aboutMyself, r.dateOfBirth, r.sex,
        r.cityId)).update((login, email, name, avatar, aboutMyself, dateOfBirth, sex, cityId)) >>
      users.filter(_.id === userId).take(1).result.head
    }
  }

}