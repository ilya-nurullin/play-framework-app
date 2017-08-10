package controllers

import javax.inject._

import models.{Project, ProjectDAO, TaskDAO}
import play.api.mvc._
import actions.Actions
import errorJsonBodies.JsonErrors
import helpers.{JsonFormHelper, LastSyncIdHelper}
import play.api.data.Forms._
import play.api.data._
import json.implicits.formats.ProjectJsonFormat._
import json.implicits.formats.TaskJsonFormat._
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProjectController @Inject() (actions: Actions, projectDAO: ProjectDAO, taskDAO: TaskDAO,
                                   lastSyncIdHelper: LastSyncIdHelper)(implicit ec: ExecutionContext) extends InjectedController {

  def get(projectId: Long) = actions.AuthAction.async { implicit request =>
    projectDAO.getProjectById(request.userId, projectId).map { projectOpt =>
      if (projectOpt.isEmpty) {
        Forbidden(JsonErrors.GettingSomeoneElsesObject)
      }
      else
        Ok(Json.toJson(projectOpt.get))
    }
  }

  def getLatestProjects(startWith: Long = 0) = actions.AuthAction.async { request =>
    projectDAO.getLatestProjects(request.userId, startWith).map { projectsOpt =>
      Ok(Json.toJson(projectsOpt))
    }
  }

  def getTasks(projectId: Long, startWith: Long = 0) = actions.AuthAction.async { request =>
    taskDAO.getLatestTasksFromProject(projectId, request.userId, startWith).map { tasksOpt =>
      Ok(Json.toJson(tasksOpt))
    }
  }

  def create(syncId: Option[Long] = None) = actions.AuthAction.async { implicit request =>
    lastSyncIdHelper.checkSyncId(syncId) {

      val jsonRequest = Form(
        mapping(
          "title" -> nonEmptyText,
          "description" -> optional(nonEmptyText),
          "isArchived" -> boolean,
          "color" -> optional(nonEmptyText(6, 6))
        )(JsonProject.apply)(JsonProject.unapply)
      )

      JsonFormHelper.asyncJsonForm(jsonRequest) { json =>
        projectDAO.createNewProject(request.userId, Project(0l, json.title, json.description, json.isArchived, json.color)
        ).flatMap { projectId =>
          projectDAO.getProjectById(request.userId, projectId).map { projectOpt =>
            Ok(Json.toJson(projectOpt))
          }
        }
      }
    }
  }

  def update(projectId: Long) = actions.AuthAction.async { implicit request =>
    val jsonRequest = Form(
      mapping(
        "title" -> nonEmptyText,
        "description" -> optional(nonEmptyText),
        "isArchived" -> boolean,
        "color" -> optional(nonEmptyText(6,6))
      )(JsonProject.apply)(JsonProject.unapply)
    )

    JsonFormHelper.asyncJsonForm(jsonRequest) { json =>
      projectDAO.isProjectOwner(request.userId, projectId).flatMap { isOwner =>
        if (isOwner)
          projectDAO.updateProject(request.userId, json.title, json.description, json.isArchived, json.color
          ).flatMap { projectId =>
            projectDAO.getProjectById(request.userId, projectId).map { projectOpt =>
              Ok(Json.toJson(projectOpt))
            }
          }
        else
          Future.successful(Forbidden(JsonErrors.ChangingSomeoneElsesObject))
      }
    }
  }

  def delete(projectId: Long) = actions.AuthAction.async { implicit request =>
    projectDAO.isProjectOwner(request.userId, projectId).flatMap { isOwner =>
      if (isOwner)
        projectDAO.deleteProject(projectId).map(_ => NoContent)
      else
        Future.successful(Forbidden(JsonErrors.DeletingSomeoneElsesObject))
    }
  }

  case class JsonProject(title: String, description: Option[String], isArchived: Boolean, color: Option[String])
}
