package controllers

import javax.inject._

import actions.Actions
import errorJsonBodies.JsonErrors
import json.implicits.formats.DateJsonFormat
import models.TaskDAO
import org.joda.time.DateTime
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaskController @Inject() (actions: Actions, taskDAO: TaskDAO) extends Controller {
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
    taskDAO.getLastTasks(request.userId, startWith).map { tasksOpt =>
      Ok(Json.toJson(tasksOpt))
    }
  }

  def create() = actions.AuthAction.async { implicit request =>
    val jsonTask = Form(
      mapping(
        "title" -> nonEmptyText,
        "description" -> optional(nonEmptyText),
        "deadline" -> optional(jodaDate(DateJsonFormat.format)),
        "importance" -> optional(number(0, 100)),
        "complexity" -> optional(number(0, 100))
      )(CreateTaskModel.apply)(CreateTaskModel.unapply)
    )

    jsonTask.bindFromRequest.fold(
      hasError => {
        val errors = hasError.errors.foldLeft(Map[String, String]()) { (m, e) => m + (e.key -> e.message) }
        Future.successful(BadRequest(JsonErrors.BadData(Json.toJson(errors))))
      },
      task => {
        taskDAO.createNewTask(request.userId, models.Task(0, task.title, task.description, task.deadline,
          (request.body.asJson.get \ "data").asOpt[JsObject], task.importance, task.complexity)
        ).flatMap { taskId =>
          taskDAO.getById(taskId, request.userId).map{ taskOpt =>
            Ok(Json.toJson(taskOpt))
          }
        }
      }
    )
  }

  def update(taskId: Long) = actions.AuthAction.async { implicit request =>

    taskDAO.isTaskOwner(taskId, request.userId).flatMap { ownerOpt =>
      if (ownerOpt.isEmpty)
        Future.successful(Forbidden(JsonErrors.ChangingSomeoneElsesObject))
      else {
        val jsonTask = Form(
          mapping(
            "title" -> nonEmptyText,
            "description" -> optional(nonEmptyText),
            "deadline" -> optional(jodaDate(DateJsonFormat.format)),
            "importance" -> optional(number(0, 100)),
            "complexity" -> optional(number(0, 100)),
            "isArchived" -> boolean
          )(UpdateTaskModel.apply)(UpdateTaskModel.unapply)
        )

        jsonTask.bindFromRequest.fold(
          hasError => {
            val errors = hasError.errors.foldLeft(Map[String, String]()) { (m, e) => m + (e.key -> e.message) }
            Future.successful(BadRequest(JsonErrors.BadData(Json.toJson(errors))))
          },
          task => {
            taskDAO.updateTask(taskId, task.title, task.description, task.deadline,
              (request.body.asJson.get \ "data").asOpt[JsObject], task.importance, task.complexity, task.isArchived).map { updatedTask =>
              Ok(Json.toJson(updatedTask))
            }
          }
        )
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

  case class CreateTaskModel(title: String, description: Option[String], deadline: Option[DateTime],
                  importance: Option[Int], complexity: Option[Int])

  case class UpdateTaskModel(title: String, description: Option[String], deadline: Option[DateTime],
                  importance: Option[Int], complexity: Option[Int], isArchived: Boolean)
}
