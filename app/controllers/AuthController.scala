package controllers

import javax.inject._

import actions.{Actions, AppIdFilterAction}
import errorJsonBodies.JsonErrors
import models.{UserDAO, UsersApiTokenDAO}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class AuthController @Inject() (userDAO: UserDAO, usersApiTokenDAO: UsersApiTokenDAO, actions: Actions) extends Controller {
  def auth() = actions.AppIdFilterAction.async { request =>
    import json.implicits.formats.DateJsonFormat._
    import org.mindrot.jbcrypt.BCrypt

    request.body.asJson.map[Future[Result]] { js =>
      val emailOpt = (js \ "email").get.asOpt[String]
      val passwordOpt = (js \ "password").get.asOpt[String]

      if (emailOpt.isEmpty || passwordOpt.isEmpty)
        Future.successful(BadRequest(JsonErrors.EmailOrPasswordNotFound))
      else {
        val email = emailOpt.get
        val password = passwordOpt.get
        userDAO.getByEmail(email).flatMap { userOpt =>
          userOpt.map { user =>
            if (BCrypt.checkpw(password, user.passHash)) {
              usersApiTokenDAO.removeOldUserAppTokens(request.appId, user.id).flatMap { _ =>
                usersApiTokenDAO.generateToken(request.appId, user.id).map { case (token, expiresAt) =>
                  Ok(Json.obj("token" -> token, "expiresAt" -> expiresAt))
                }
              }
            }
            else
              Future.successful(BadRequest(JsonErrors.BadCredentials))
          } getOrElse Future.successful(BadRequest(JsonErrors.BadCredentials))
        }
      }

    } getOrElse Future.successful(BadRequest(JsonErrors.JsonExpected))
  }

  def logout() = actions.AuthAction.async { request =>
    usersApiTokenDAO.removeOldUserAppTokens(request.appId, request.userId).map { _ =>
      NoContent
    }
  }
}
