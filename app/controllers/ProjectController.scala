package controllers

import javax.inject._

import models.{Project, ProjectDAO}
import play.api.mvc._
import actions.Actions
import errorJsonBodies.JsonErrors
import helpers.JsonFormHelper
import play.api.data.Forms._
import play.api.data._
import json.implicits.formats.ProjectJsonFormat._
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ProjectController @Inject() (actions: Actions, projectDAO: ProjectDAO) extends Controller {

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

  def create() = actions.AuthAction.async { implicit request =>
    val jsonRequest = Form(
      mapping(
        "title" -> nonEmptyText,
        "description" -> optional(nonEmptyText),
        "isArchived" -> boolean,
        "color" -> optional(nonEmptyText(6,6))
      )(JsonProject.apply)(JsonProject.unapply)
    )

    JsonFormHelper.asyncJsonForm(jsonRequest) { json =>
      projectDAO.createNewProject(request.userId, Project(0l, json.title, json.description, json.isArchived, json.color)
      ).flatMap { projectId =>
        projectDAO.getProjectById(request.userId, projectId).map { projectOpt =>
            Ok(Json.toJson(projectOpt.get))
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
              Ok(Json.toJson(projectOpt.get))
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
