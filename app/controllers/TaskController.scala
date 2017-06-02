package controllers

import javax.inject._

import actions.Actions
import models.TaskDAO
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

class TaskController @Inject() (actions: Actions, taskDAO: TaskDAO) extends Controller {
  import json.implicits.formats.TaskJsonFormat._

  def get(taskId: Int) = actions.AuthAction.async { request =>
    taskDAO.getById(taskId, request.userId).map { taskOpt =>
      Ok(Json.toJson(taskOpt))
    }
  }

  /*def create() = actions.AuthAction.async { requst =>
    Json.
  }*/
}
