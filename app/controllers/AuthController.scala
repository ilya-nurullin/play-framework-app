package controllers

import javax.inject._

import actions.Actions
import errorJsonBodies.JsonErrors
import helpers.JsonFormHelper
import json.implicits.formats.DateJsonFormat._
import models.{UserDAO, UsersApiTokenDAO}
import oauth._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class AuthController @Inject() (userDAO: UserDAO, usersApiTokenDAO: UsersApiTokenDAO, actions: Actions)(implicit ec: ExecutionContext)
    extends InjectedController {

  def emailAuth() = actions.AppIdFilterAction.async { implicit request =>
    import json.implicits.formats.DateJsonFormat._
    import org.mindrot.jbcrypt.BCrypt

    val jsonRequest = Form(
      tuple(
        "email" -> email,
        "password" -> nonEmptyText
      )
    )

    JsonFormHelper.asyncJsonForm(jsonRequest) { emailPassword =>
      val email = emailPassword._1
      val password = emailPassword._2

      userDAO.getByEmail(email).flatMap { userOpt =>
        userOpt.map { user =>
          if (BCrypt.checkpw(password, user.passHash)) {
            usersApiTokenDAO.removeOldUserAppTokens(request.appId, user.id).flatMap { _ =>
              usersApiTokenDAO.generateToken(request.appId, user.id).map { case (token, _) =>
                Ok(Json.obj("token" -> token))
              }
            }
          }
          else
            Future.successful(BadRequest(JsonErrors.BadCredentials))
        } getOrElse Future.successful(BadRequest(JsonErrors.BadCredentials))
      }
    }
  }

  def logout() = actions.AuthAction.async { request =>
    usersApiTokenDAO.removeOldUserAppTokens(request.appId, request.userId).map { _ =>
      NoContent
    }
  }
}
