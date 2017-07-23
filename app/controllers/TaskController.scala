package controllers

import javax.inject._

import actions.Actions
import errorJsonBodies.JsonErrors
import helpers.JsonFormHelper
import json.implicits.formats.DateJsonFormat
import models.{ProjectDAO, TaskDAO}
import org.joda.time.DateTime
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import play.api.data.JodaForms._

class TaskController @Inject() (actions: Actions, taskDAO: TaskDAO, projectDAO: ProjectDAO)(implicit ec: ExecutionContext)
    extends InjectedController {
  import json.implicits.formats.TaskJsonFormat._

  def get(taskId: Long) = actions.AuthAction.async { request =>
    taskDAO.getById(taskId, request.userId).map { taskOpt =>
      if (taskOpt.isEmpty)
        Forbidden(JsonErrors.GettingSomeoneElsesObject)
      else
        Ok(Json.toJson(taskOpt))
    }
  }

  def getLatestTasks(startWith: Long = 0) = actions.AuthAction.async { request =>
    taskDAO.getLatestTasks(request.userId, startWith).map { tasksOpt =>
      Ok(Json.toJson(tasksOpt))
    }
  }

  def create() = actions.AuthAction.async { implicit request =>
    val jsonTask = Form(
      mapping(
        "title" -> nonEmptyText,
        "projectId" -> longNumber,
        "description" -> optional(nonEmptyText),
        "deadline" -> optional(jodaDate(DateJsonFormat.format)),
        "importance" -> optional(number(0, 100)),
        "complexity" -> optional(number(0, 100)),
        "isArchived" -> default(boolean, false)
      )(TaskModel.apply)(TaskModel.unapply)
    )

    JsonFormHelper.asyncJsonForm(jsonTask){ task =>
      projectDAO.isProjectOwner(request.userId, task.projectId).flatMap { isOwner =>
        if (isOwner)
          taskDAO.createNewTask(request.userId, models.Task(0, task.title, task.projectId, task.description, task.deadline,
            (request.body.asJson.get \ "data").asOpt[JsObject], task.importance, task.complexity)
          ).flatMap { taskId =>
            taskDAO.getById(taskId, request.userId).map { taskOpt =>
              Ok(Json.toJson(taskOpt))
            }
          }
        else
          Future.successful(Forbidden(JsonErrors.BadDataNonOwner("projectId")))
      }
    }
  }

  def update(taskId: Long) = actions.AuthAction.async { implicit request =>

    taskDAO.isTaskOwner(taskId, request.userId).flatMap { ownerOpt =>
      if (ownerOpt.isEmpty)
        Future.successful(Forbidden(JsonErrors.ChangingSomeoneElsesObject))
      else {
        val jsonTask = Form(
          mapping(
            "title" -> nonEmptyText,
            "projectId" -> longNumber,
            "description" -> optional(nonEmptyText),
            "deadline" -> optional(jodaDate(DateJsonFormat.format)),
            "importance" -> optional(number(0, 100)),
            "complexity" -> optional(number(0, 100)),
            "isArchived" -> boolean
          )(TaskModel.apply)(TaskModel.unapply)
        )

        JsonFormHelper.asyncJsonForm(jsonTask) { task =>
          projectDAO.isProjectOwner(request.userId, task.projectId).flatMap { isOwner =>
            if (isOwner)
              taskDAO.updateTask(taskId, task.title, task.projectId, task.description, task.deadline,
                (request.body.asJson.get \ "data").asOpt[JsObject], task.importance, task.complexity, task.isArchived).map {
                updatedTask =>
                  Ok(Json.toJson(updatedTask))
              }
            else
              Future.successful(Forbidden(JsonErrors.BadDataNonOwner("projectId")))
          }
        }

      }
    }
  }

  def delete(taskId: Long) = actions.AuthAction.async { implicit request =>
    taskDAO.isTaskOwner(taskId, request.userId).flatMap { ownerOpt =>
      if (ownerOpt.isEmpty)
        Future.successful(Forbidden(JsonErrors.DeletingSomeoneElsesObject))
      else {
        taskDAO.deleteTask(taskId).map { deletedNum =>
          if (deletedNum > 0)
            NoContent
          else
            NotFound
        }
      }
    }
  }

  case class CreateTaskModel(title: String, projectId: Long, description: Option[String], deadline: Option[DateTime],
                  importance: Option[Int], complexity: Option[Int])

  case class TaskModel(title: String, projectId: Long, description: Option[String], deadline: Option[DateTime],
                       importance: Option[Int], complexity: Option[Int], isArchived: Boolean)
}
