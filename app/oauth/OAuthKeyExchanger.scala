package oauth


import javax.inject._

import models.{UserDAO, UsersApiTokenDAO}
import org.joda.time.DateTime
import play.api.libs.json.JsArray
import play.api.libs.ws._

import scala.concurrent.Future

@Singleton
class OAuthKeyExchanger @Inject()(ws: WSClient, userDAO: UserDAO, usersApiTokenDAO: UsersApiTokenDAO) {
  import scala.concurrent.ExecutionContext.Implicits.global

  case class OAuthCredentials(email: String, token: String)
  case class OAuthResult(token: String, expiresAt: DateTime, actionType: String)

  sealed abstract class ActionType(val userId: Int)
  case class Reg(override val userId: Int) extends ActionType(userId)
  case class Login(override val userId: Int) extends ActionType(userId)

  def exchangeToken(network: String, credentials: OAuthCredentials, appId: Int): Future[OAuthResult] = {
    (network match {
      case "facebook" => Facebook(credentials)
      case _ => Future.failed(new OAuthNetworkNotFoundException)
    }).flatMap { actionType =>
      usersApiTokenDAO.removeOldUserAppTokens(appId, actionType.userId).flatMap { _ =>
        usersApiTokenDAO.generateToken(appId, actionType.userId).map { tokenPair =>
          actionType match {
            case _: Reg => OAuthResult(tokenPair._1, tokenPair._2, "registration")
            case _: Login => OAuthResult(tokenPair._1, tokenPair._2, "login")
          }
        }
      }
    }
  }

  private def isSocNetworkAllowedByUser(name: String, socNetworks: Option[JsArray]): Boolean = {
    if (socNetworks.isEmpty)
      false
    else
      socNetworks.get.value.map(_.as[String]).contains(name)
  }

  object Facebook {
    val socName = "facebook"

    def apply(credentials: OAuthCredentials): Future[ActionType] = {
      ws.url("https://graph.facebook.com/v2.9/me").
        withQueryStringParameters("fields" -> "email", "access_token" -> credentials.token).get()
        .flatMap { response =>
          val jsResponse = response.json
          val profileEmail = (jsResponse \ "email").asOpt[String]
          if (profileEmail.isEmpty)
             Future.failed(new OAuthEmptyEmailException)
          else if (profileEmail.get != credentials.email)
            throw new OAuthNonEqualEmailException
          else if (profileEmail.get == credentials.email) {
            userDAO.getByEmail(profileEmail.get).flatMap { userOpt =>
              if (userOpt.isEmpty) {
                userDAO.create(profileEmail.get, "   ").map { userId =>
                  userDAO.allowNetwork(socName, userId)
                  Reg(userId)
                }
              }
              else if (isSocNetworkAllowedByUser(socName, userOpt.get.socNetworks)) {
                Future.successful(Login(userOpt.get.id))
              }
              else {
                Future.failed(new OAuthNetworkNotAllowedByUserException)
              }
            }
          }
          else
            Future.failed(new Exception(s"OAuth $socName"))
      }
    }
  }
}

class OAuthEmptyEmailException extends Exception
class OAuthNonEqualEmailException extends Exception
class OAuthNetworkNotFoundException extends Exception
class OAuthNetworkNotAllowedByUserException extends Exception
