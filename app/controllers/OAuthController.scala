package controllers

import javax.inject._

import actions.Actions
import errorJsonBodies.JsonErrors
import helpers.JsonFormHelper
import models._
import oauth._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.i18n.I18nSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OAuthController @Inject() (actions: Actions, userHasSocialNetworkDAO: UserHasSocialNetworkDAO,
                                 oAuthChecker: OAuthChecker, userDAO: UserDAO, usersApiTokenDAO: UsersApiTokenDAO,
                                 projectDAO: ProjectDAO, taskDAO: TaskDAO)
                                (implicit ec: ExecutionContext)
extends InjectedController with I18nSupport {

  def registration(networkName: String) = actions.AppIdFilterAction.async { implicit request =>
    val jsonRequest = Form(
      tuple(
        "userNetworkId" -> nonEmptyText,
        "token" -> nonEmptyText
      )
    )

    JsonFormHelper.asyncJsonForm(jsonRequest) {
      case (userNetworkId, token) =>
        oAuthChecker.checkUserNetworkIdAndToken(networkName, OAuthCredentials(userNetworkId, token)).flatMap {
          case oAuthSuccess: OAuthSuccess => userHasSocialNetworkDAO.isUserNetworkIdAlreadySignedUp(userNetworkId).flatMap { isAlreadySignedUp =>

            if (isAlreadySignedUp)
              Future.successful(BadRequest(JsonErrors.OAuthAlreadySignedUp))
            else {
              val emailOpt = oAuthSuccess.email
              if (emailOpt.isEmpty)
                Future.successful(BadRequest(JsonErrors.OAuthEmptyEmail))
              else {
                val email = emailOpt.get

                // check whether user signed up with different oauth network with the same email or not
                userHasSocialNetworkDAO.findUserByEmail(email).flatMap { userOpt =>
                  userOpt.map { _ =>
                    // user signed up with different oauth network with the same email
                    Future.successful(Conflict(JsonErrors.OAuthEmailConflict))
                  } getOrElse {
                    // user DID NOT sign up with different oauth network with the same email
                    // Sign up new user
                    for {
                      userId <- userDAO.create(email, "   ")
                      _ <- userHasSocialNetworkDAO.addSocialNetworkToUser(UserHasSocialNetwork(userId, networkName, userNetworkId, emailOpt))
                      whipcakeTokenTuple <- usersApiTokenDAO.generateToken(request.appId, userId)
                    } yield {
                      projectDAO.createDefaultProject(userId).map(taskDAO.createGreetingTasks(userId, _))
                      Ok(Json.obj("token" -> whipcakeTokenTuple._1, "userId" -> userId))
                    }
                  }
                }
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
        "userNetworkId" -> nonEmptyText,
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
                  Ok(Json.obj("token" -> whipcakeTokenTuple._1, "userId" -> userId))
                }
              } getOrElse Future.successful(BadRequest(JsonErrors.OAuthFailed))
            }
          case OAuthFailed => Future.successful(BadRequest(JsonErrors.OAuthFailed))
        }
    }
  }
}