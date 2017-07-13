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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class AuthController @Inject() (userDAO: UserDAO, usersApiTokenDAO: UsersApiTokenDAO, actions: Actions, oauthExchanger: OAuthKeyExchanger) extends Controller {

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
  }

  def oauth(network: String) = actions.AppIdFilterAction.async { implicit request =>
    val jsonRequest = Form(
      tuple(
        "email" -> email,
        "token" -> nonEmptyText
      )
    )

    JsonFormHelper.asyncJsonForm(jsonRequest) { json =>
      oauthExchanger.exchangeToken(network, oauthExchanger.OAuthCredentials(json._1, json._2), request.appId).map {
        oauthResult =>
          Ok(Json.obj("token" -> oauthResult.token, "expiresAt" -> oauthResult.expiresAt,
            "actionType" -> oauthResult.actionType))
      }.recover {
        case _: OAuthEmptyEmailException => BadRequest(JsonErrors.OAuthEmptyEmail)
        case _: OAuthNonEqualEmailException => BadRequest(JsonErrors.OAuthNonEqualEmail)
        case _: OAuthNetworkNotFoundException => BadRequest(JsonErrors.OAuthNetworkNotFound)
        case _: OAuthNetworkNotAllowedByUserException => BadRequest(JsonErrors.OAuthNetworkNotAllowedByUser)
      }
    }
  }

  def logout() = actions.AuthAction.async { request =>
    usersApiTokenDAO.removeOldUserAppTokens(request.appId, request.userId).map { _ =>
      NoContent
    }
  }
}
