package models

import javax.inject._

import com.github.tototoshi.slick.MySQLJodaSupport._
import json.implicits.JsObjectMappedColumn
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Messages
import play.api.libs.json.JsObject
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{TableQuery, Tag}

import scala.concurrent.ExecutionContext

case class Task(id: Long, title: String, projectId: Long, description: Option[String] = None, deadline: Option[DateTime] = None,
                data: Option[JsObject] = None, importance: Option[Int] = None, complexity: Option[Int] = None,
                createdAt: DateTime = DateTime.now(), updatedAt: DateTime = DateTime.now(), isArchived: Boolean = false)

class TaskTable(tag: Tag) extends Table[Task](tag, "tasks") with JsObjectMappedColumn {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def title = column[String]("title")
  def projectId = column[Long]("project_id")
  def description = column[Option[String]]("description")
  def deadline = column[Option[DateTime]]("deadline")
  def data = column[Option[JsObject]]("data")
  def importance = column[Option[Int]]("importance")
  def complexity = column[Option[Int]]("complexity")
  def createdAt = column[DateTime]("created_at")
  def updatedAt = column[DateTime]("updated_at")
  def isArchived = column[Boolean]("is_archived")

  def * = (id, title, projectId, description, deadline, data, importance, complexity, createdAt, updatedAt, isArchived) <>
    (Task.tupled, Task.unapply)
}

case class UserHasTask(userId: Int, taskId: Long)

class UserHasTaskTable(tag: Tag) extends Table[UserHasTask](tag, "user_has_task") {
  def userId = column[Int]("user_id")
  def taskId = column[Long]("task_id")

  def * = (userId, taskId) <> (UserHasTask.tupled, UserHasTask.unapply)
}

@Singleton
class TaskDAO @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends JsObjectMappedColumn {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  val tasks = TableQuery[TaskTable]
  val usersHasTasks = TableQuery[UserHasTaskTable]

  val tasksCount = 10

  def getById(taskId: Long, userId: Int) = dbConfig.db.run {
    tasks.
      join(usersHasTasks).
      on (_.id === _.taskId).
      filter(_._2.userId === userId).
      filter(_._1.id === taskId).
      map(_._1).
      take(1).
      result.headOption
  }

  def getLatestTasks(userId: Int, startWith: Long = 0) = dbConfig.db.run {
    tasks.
      join(usersHasTasks).
      on(_.id === _.taskId).
      filter(_._2.userId === userId).
      map(_._1).
      filter(r => if (startWith > 0) r.id < startWith else true.bind).
      sortBy(_.id.desc).
      take(tasksCount).
      result
  }

  def getLatestTasksFromProject(projectId: Long, userId: Int, startWith: Long = 0) = dbConfig.db.run {
    tasks.
      join(usersHasTasks).
      on(_.id === _.taskId).
      filter(_._2.userId === userId).
      map(_._1).
      filter(r => if (startWith > 0) r.id < startWith else true.bind).
      filter(_.projectId === projectId).
      sortBy(_.id.desc).
      take(tasksCount).
      result
  }

  def createNewTask(userId: Int, task: Task) = dbConfig.db.run {
    for {
      newTaskId <- (tasks returning tasks.map(_.id)) += task
      _ <- usersHasTasks += UserHasTask(userId, newTaskId)
    } yield newTaskId
  }

  def isTaskOwner(taskId: Long, userId: Int) = dbConfig.db.run {
    usersHasTasks.filter(r => r.userId === userId && r.taskId === taskId).take(1).result.headOption
  }

  def updateTask(taskId: Long,  title: String, projectId: Long, description: Option[String], deadline: Option[DateTime],
                 data: Option[JsObject], importance: Option[Int], complexity: Option[Int], isArchived: Boolean) = {
    dbConfig.db.run {
      tasks.filter(_.id === taskId).
        map(r => (r.title, r.projectId, r.description, r.deadline, r.data, r.importance, r.complexity, r.isArchived)).
        update((title, projectId, description, deadline, data, importance, complexity, isArchived)
        ) >>
          tasks.filter(_.id === taskId).take(1).result.head
    }
  }

  def deleteTask(taskId: Long) = dbConfig.db.run {
    tasks.filter(_.id === taskId).delete
  }

  def createGreetingTasks(userId: Int, projectId: Long)(implicit messages: Messages) = {
    createNewTask(userId, Task(0, messages("tasks.greetingTasks.first.title"), projectId))
    createNewTask(userId, Task(0, messages("tasks.greetingTasks.second.title"), projectId))
    createNewTask(userId, Task(0, messages("tasks.greetingTasks.third.title"), projectId))
  }

}

