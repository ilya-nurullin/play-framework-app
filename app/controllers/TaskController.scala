package controllers

import javax.inject._

import actions.Actions
import errorJsonBodies.JsonErrors
import helpers.{JsonFormHelper, LastSyncIdHelper}
import json.implicits.formats.DateTimeJsonFormat
import models.{ProjectDAO, TaskDAO}
import org.joda.time.DateTime
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import play.api.data.JodaForms._

@Singleton
class TaskController @Inject()(actions: Actions, taskDAO: TaskDAO, projectDAO: ProjectDAO,
                               lastSyncIdHelper: LastSyncIdHelper)(implicit ec: ExecutionContext)
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

  def create(syncId: Option[Long] = None) = actions.AuthAction.async { implicit request =>
    lastSyncIdHelper.checkSyncId(syncId) {

      val jsonTask = Form(
        mapping(
          "title" -> nonEmptyText,
          "projectId" -> longNumber,
          "description" -> optional(nonEmptyText),
          "deadline" -> optional(jodaDate(DateTimeJsonFormat.dateTimeFormat)),
          "importance" -> optional(number(0, 100)),
          "complexity" -> optional(number(0, 100)),
          "isOpenForSurety" -> boolean,
          "isArchived" -> default(boolean, false)
        )(TaskModel.apply)(TaskModel.unapply)
      )

      JsonFormHelper.asyncJsonForm(jsonTask) { task =>
        projectDAO.isProjectOwner(request.userId, task.projectId).flatMap { isOwner =>
          if (isOwner)
            taskDAO.createNewTask(request.userId, models.Task(0, task.title, task.projectId, task.description, task.deadline,
              (request.body.asJson.get \ "data").asOpt[JsObject], task.importance, task.complexity,
              isOpenForSurety = task.isOpenForSurety, isArchived = task.isArchived)
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
  }

  def update(taskId: Long) = actions.AuthAction.async { implicit request =>

    taskDAO.isTaskOwner(taskId, request.userId).flatMap { isOwner =>
      if (! isOwner)
        Future.successful(Forbidden(JsonErrors.ChangingSomeoneElsesObject))
      else {
        val jsonTask = Form(
          mapping(
            "title" -> nonEmptyText,
            "projectId" -> longNumber,
            "description" -> optional(nonEmptyText),
            "deadline" -> optional(jodaDate(DateTimeJsonFormat.dateTimeFormat)),
            "importance" -> optional(number(0, 100)),
            "complexity" -> optional(number(0, 100)),
            "isOpenForSurety" -> boolean,
            "isArchived" -> boolean
          )(TaskModel.apply)(TaskModel.unapply)
        )

        JsonFormHelper.asyncJsonForm(jsonTask) { task =>
          projectDAO.isProjectOwner(request.userId, task.projectId).flatMap { isOwner =>
            if (isOwner)
              taskDAO.updateTask(taskId, task.title, task.projectId, task.description, task.deadline,
                (request.body.asJson.get \ "data").asOpt[JsObject], task.importance, task.complexity, task.isOpenForSurety,
                  task.isArchived).map {
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
    taskDAO.isTaskOwner(taskId, request.userId).flatMap { isOwner =>
      if (! isOwner)
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

  case class TaskModel(title: String, projectId: Long, description: Option[String], deadline: Option[DateTime],
                       importance: Option[Int], complexity: Option[Int], isOpenForSurety: Boolean, isArchived: Boolean)
}
