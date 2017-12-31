package controllers

import javax.inject._

import actions.Actions
import errorJsonBodies.JsonErrors
import helpers.{FirebaseHelper, JsonFormHelper}
import json.implicits.formats.DateTimeJsonFormat._
import models.surety.{Surety, SuretyDAO, SuretyTable, WhipDAO}
import models.{TaskDAO, UserDAO, UsersApiTokenDAO}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SuretyController @Inject()(actions: Actions, suretyDAO: SuretyDAO, taskDAO: TaskDAO, whipDAO: WhipDAO, userDAO: UserDAO,
                                 val firebaseHelper: FirebaseHelper, usersApiTokenDAO: UsersApiTokenDAO)
                                (implicit ec: ExecutionContext) extends InjectedController {
  implicit val fullUserFormat = Json.format[UserController.FullUserJson]

  def topGuarantors() = actions.AuthAction.async { request =>
    for {
      guarantors <- suretyDAO.getTopGuarantors()
    } yield Ok(Json.toJson(guarantors.map(UserController.userRow2FullJsonUser)))
  }

  def sendSuretyRequest(taskId: Long) = actions.AuthAction.async { implicit request =>
    val jsonRequest = Form(
      tuple(
        "guarantors" -> set(number),
        "timeFrom" -> optional(localTime),
        "timeTo" -> optional(localTime),
        "allowedCount" -> optional(byteNumber)
      )
    )

    JsonFormHelper.asyncJsonForm(jsonRequest) { jsReqTuple =>
      val guarantors = jsReqTuple._1
      val timeFrom = jsReqTuple._2.map(r => new org.joda.time.LocalTime(r.toString))
      val timeTo = jsReqTuple._3.map(r => new org.joda.time.LocalTime(r.toString))
      val allowedCount = jsReqTuple._4

      if (guarantors.contains(request.userId))
        Future.successful(BadRequest(JsonErrors.GuarantorToMyself))
      else
        taskDAO.getById(taskId, request.userId).flatMap { taskOpt  =>
          if (taskOpt.isEmpty)
            Future.successful(Forbidden(JsonErrors.ChangingSomeoneElsesObject))
          else {
            if (taskOpt.get.isOpenForSurety) {
              suretyDAO.addSurety(Surety(taskId, timeFrom = timeFrom, timeTo = timeTo, allowedCount = allowedCount))
              sendSuretyRequestNotification(guarantors)
              suretyDAO.addSuretyRequest(taskId, guarantors).map(_ => NoContent)
            }
            else
              Future.successful(BadRequest(JsonErrors.TaskIsNotOpenForSurety))
          }
        }
    }
  }

  def approveSuretyRequest(taskId: Long) = actions.AuthAction.async { implicit request =>
    suretyDAO.isRequestedGuarantor(taskId, request.userId).flatMap { isRequestedGuarantor =>
      if (! isRequestedGuarantor)
        Future.successful(Forbidden(JsonErrors.ChangingSomeoneElsesObject))
      else {
        for {
          _ <- suretyDAO.approveSurety(taskId, request.userId)
          _ <- suretyDAO.updateSuretyGuarantorWithStatus(taskId, request.userId, SuretyTable.Executing)
          _ <- sendApprovedResponse(taskId)
        } yield NoContent
      }
    }
  }

  def whip(taskId: Long) = actions.AuthAction.async { implicit request =>
    val jsRequest = Form(
      tuple(
        "message" -> optional(nonEmptyText),
        "image" -> optional(nonEmptyText)
      )
    )

    suretyDAO.isGuarantor(taskId, request.userId).flatMap { isGuarantor =>
      if (! isGuarantor)
        Future.successful(BadRequest(JsonErrors.ChangingSomeoneElsesObject))
      else {
        JsonFormHelper.asyncJsonForm(jsRequest) { jsTuple =>
          sendWhip(taskId, jsTuple._2)
          whipDAO.whipTo(taskId, request.userId, jsTuple._1, jsTuple._2).map { _ => NoContent }
        }
      }
    }
  }

  def sendSuretyRequestNotification(guarantors: Set[Int]) = Future {
    guarantors.map { guarantorId =>
      userDAO.getById(guarantorId).flatMap { userOpt =>
        val user = userOpt.get
        usersApiTokenDAO.getAllTokensByUserId(user.id).map { userTokens =>
          userTokens.map { userToken =>
            firebaseHelper.sendSuretyRequest(userToken.firebaseToken, user.lang)
          }
        }
      }
    }
  }

  def sendApprovedResponse(taskId: Long) = {
    taskDAO.getAllTaskOwners(taskId).map { taskOwners =>
      taskOwners.map { taskOwnerId =>
        userDAO.getById(taskOwnerId).flatMap { userOpt =>
          usersApiTokenDAO.getAllTokensByUserId(userOpt.get.id).map { userTokens =>
            userTokens.map { userToken =>
              firebaseHelper.sendApproveRequest(userToken.firebaseToken, userOpt.get.lang)
            }
          }
        }
      }
    }
  }

  def sendWhip(taskId: Long, message: Option[String]) = {
    taskDAO.getAllTaskOwners(taskId).map { taskOwners =>
      taskOwners.map { taskOwnerId =>
        userDAO.getById(taskOwnerId).flatMap { userOpt =>
          usersApiTokenDAO.getAllTokensByUserId(userOpt.get.id).map { userTokens =>
            userTokens.map { userToken =>
              firebaseHelper.sendWhip(userToken.firebaseToken, message, userOpt.get.lang)
            }
          }
        }
      }
    }
  }
}
