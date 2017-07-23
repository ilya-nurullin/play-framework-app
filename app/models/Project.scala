package models

import javax.inject._

import com.github.tototoshi.slick.MySQLJodaSupport._
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{TableQuery, Tag}

import scala.concurrent.ExecutionContext

case class Project(id: Long, title: String, description: Option[String], isArchived: Boolean, color: Option[String],
                   createdAt: Option[DateTime] = Some(DateTime.now()), updatedAt: Option[DateTime] = Some(DateTime.now()))

class ProjectTable(tag: Tag) extends Table[Project](tag, "projects") {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def title = column[String]("title")
  def description = column[Option[String]]("description")
  def isArchived = column[Boolean]("is_archived")
  def color = column[Option[String]]("color")
  def createdAt = column[Option[DateTime]]("created_at")
  def updatedAt = column[Option[DateTime]]("updated_at")

  def * = (id, title, description, isArchived, color, createdAt, updatedAt) <> (Project.tupled, Project.unapply)
}

case class UserHasProject(userId: Int, projectId: Long)

class UserHasProjectTable(tag: Tag) extends Table[UserHasProject](tag, "user_has_project") {
  def userId = column[Int]("user_id")
  def projectId = column[Long]("project_id")

  def * = (userId, projectId) <> (UserHasProject.tupled, UserHasProject.unapply)
}

@Singleton
class ProjectDAO @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  val projects = TableQuery[ProjectTable]
  val usersHasProjects = TableQuery[UserHasProjectTable]

  def getProjectById(userId: Int, projectId: Long) = dbConfig.db.run {
    projects.
      join(usersHasProjects).
      on ((p, up) => p.id === up.projectId && up.userId === userId).
      filter(_._1.id === projectId).
      map(_._1).
      take(1).
      result.headOption
  }

  def getLatestProjects(userId: Int, startWith: Long = 0) = dbConfig.db.run {
    val projectCount = 10

    projects.
      join(usersHasProjects).
      on(_.id === _.projectId).
      filter(_._2.userId === userId).
      map(_._1).
      filter(r => if (startWith > 0) r.id < startWith else true.bind).
      sortBy(_.id.desc).
      take(projectCount).
      result
  }

  def createNewProject(userId: Int, project: Project) = dbConfig.db.run {
    for {
      newProjectId <- (projects returning projects.map(_.id)) += project
      _ <- usersHasProjects += UserHasProject(userId, newProjectId)
    } yield newProjectId
  }

  def updateProject(projectId: Long, title: String, description: Option[String], isArchived: Boolean,
                    color: Option[String]) = {
    dbConfig.db.run {
      projects.filter(_.id === projectId).map(r => (r.title, r.description, r.isArchived, r.color)).update(
        (title, description, isArchived, color)
      )
    }
  }

  def isProjectOwner(userId: Int, projectId: Long) = dbConfig.db.run {
    usersHasProjects.filter(r => r.userId === userId && r.projectId === projectId).take(1).result.headOption.collect {
      case pr => pr.isDefined
    }
  }

  def deleteProject(projectId: Long) = dbConfig.db.run {
    projects.filter(_.id === projectId).delete
  }
}