package controllers

import javax.inject._

import actions.Actions
import errorJsonBodies.JsonErrors
import helpers.JsonFormHelper
import models.{UserDAO, UserHasSocialNetwork, UserHasSocialNetworkDAO, UsersApiTokenDAO}
import oauth._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OAuthController @Inject() (actions: Actions, userHasSocialNetworkDAO: UserHasSocialNetworkDAO,
                                 oAuthChecker: OAuthChecker, userDAO: UserDAO, usersApiTokenDAO: UsersApiTokenDAO)
                                (implicit ec: ExecutionContext)
extends InjectedController {

  def registration(networkName: String) = actions.AppIdFilterAction.async { implicit request =>
    val jsonRequest = Form(
      tuple(
        "userNetworkId" -> longNumber(0),
        "token" -> nonEmptyText
      )
    )

    JsonFormHelper.asyncJsonForm(jsonRequest) {
      case (userNetworkId, token) =>
        oAuthChecker.checkUserNetworkIdAndToken(networkName, OAuthCredentials(userNetworkId, token)).flatMap {
          case res: OAuthSuccess => userHasSocialNetworkDAO.isUserNetworkIdAlreadySignedUp(userNetworkId).flatMap { isAlreadySignedUp =>

            if (isAlreadySignedUp)
              Future.successful(BadRequest(JsonErrors.OAuthAlreadySignedUp))
            else {
              val emailOpt = res.email
              if (emailOpt.isEmpty)
                Future.successful(BadRequest(JsonErrors.OAuthEmptyEmail))
              else {
                for {
                  userId <- userDAO.create(emailOpt.get, "   ")
                  _ <- userHasSocialNetworkDAO.addSocialNetworkToUser(UserHasSocialNetwork(userId, networkName, userNetworkId, emailOpt))
                  whipcakeTokenTuple <- usersApiTokenDAO.generateToken(request.appId, userId)
                } yield Ok(Json.obj("token" -> whipcakeTokenTuple._1))
              }
            }

          }
          case OAuthFailed => Future.successful(BadRequest(JsonErrors.OAuthFailed))
        }
    }
  }

  def auth(networkName: String) = actions.AppIdFilterAction.async { implicit request =>
    val jsonRequest = Form(
      tuple(
        "userNetworkId" -> longNumber(0),
        "token" -> nonEmptyText
      )
    )

    JsonFormHelper.asyncJsonForm(jsonRequest) {
      case (userNetworkId, token) =>
        oAuthChecker.checkUserNetworkIdAndToken(networkName, OAuthCredentials(userNetworkId, token)).flatMap {
          case res: OAuthSuccess =>
            userHasSocialNetworkDAO.getUserIdByNetworkNameAndNetworkUserId(networkName, userNetworkId).flatMap { userIdOpt =>
              userIdOpt.map { userId =>
                val emailOpt = res.email

                if (emailOpt.isDefined)
                  userHasSocialNetworkDAO.updateUsersNetworkEmail(userId, networkName, emailOpt.get)

                usersApiTokenDAO.generateToken(request.appId, userId).map { whipcakeTokenTuple =>
                  Ok(Json.obj("token" -> whipcakeTokenTuple._1))
                }
              } getOrElse Future.successful(BadRequest(JsonErrors.OAuthFailed))
            }
          case OAuthFailed => Future.successful(BadRequest(JsonErrors.OAuthFailed))
        }
    }
  }
}