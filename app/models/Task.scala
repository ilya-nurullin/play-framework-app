package models

import javax.inject._

import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{TableQuery, Tag}
import com.github.tototoshi.slick.MySQLJodaSupport._

import scala.concurrent.Future

case class Task(id: Long, title: String, description: Option[String] = None, deadline: Option[DateTime] = None,
                data: Option[String] = None, importance: Option[Int] = None, complexity: Option[Int] = None,
                createdAt: DateTime = DateTime.now(), updatedAt: DateTime = DateTime.now(), isArchived: Boolean = false)

class TaskTable(tag: Tag) extends Table[Task](tag, "tasks") {
  def id = column[Long]("id", O.AutoInc)
  def title = column[String]("title")
  def description = column[Option[String]]("description")
  def deadline = column[Option[DateTime]]("deadline")
  def data = column[Option[String]]("data")
  def importance = column[Option[Int]]("importance")
  def complexity = column[Option[Int]]("complexity")
  def createdAt = column[DateTime]("created_at")
  def updatedAt = column[DateTime]("updated_at")
  def isArchived = column[Boolean]("is_archived")

  def * = (id, title, description, deadline, data, importance, complexity, createdAt, updatedAt, isArchived) <>
    (Task.tupled, Task.unapply)
}

case class UserHasTask(userId: Int, taskId: Long)

class UserHasTaskTable(tag: Tag) extends Table[UserHasTask](tag, "user_has_task") {
  def userId = column[Int]("user_id")
  def taskId = column[Long]("task_id")

  def * = (userId, taskId) <> (UserHasTask.tupled, UserHasTask.unapply)
}

class TaskDAO @Inject() (dbConfigProvider: DatabaseConfigProvider) {
  import scala.concurrent.ExecutionContext.Implicits.global
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  val tasks = TableQuery[TaskTable]
  val usersHasTasks = TableQuery[UserHasTaskTable]

  def getById(taskId: Long, userId: Int) = dbConfig.db.run {
    val query = for {
      (t, _) <- tasks join usersHasTasks on (_.id === _.taskId) filter (_._2.userId === userId)
    } yield t
    query.take(1).result.headOption
  }

  def createNewTask(userId: Int, task: Task) = dbConfig.db.run {
      (tasks returning tasks.map(_.id)) += task
  }.map{ newTaskId =>
    dbConfig.db.run {
      DBIO.seq(usersHasTasks += UserHasTask(userId, newTaskId))
    }
    newTaskId
  }

}

