package oauth

import javax.inject._

import models.{UserDAO, UsersApiTokenDAO}
import play.api.libs.ws._

import scala.concurrent.{ExecutionContext, Future}

case class OAuthCredentials(userNetworkId: Long, token: String)

abstract class OAuthResult
object OAuthFailed extends OAuthResult
case class OAuthSuccess(userNetworkId: Long, token: String, email: Option[String]) extends OAuthResult

@Singleton
class OAuthChecker @Inject()(ws: WSClient, userDAO: UserDAO, usersApiTokenDAO: UsersApiTokenDAO)(implicit val ex: ExecutionContext) {

  def checkUserNetworkIdAndToken(networkName: String, credentials: OAuthCredentials) = {
    networkName match {
      case "facebook" => Facebook(credentials)
      case _ => Future.failed(new OAuthNetworkNotFoundException)
    }
  }


  private object Facebook {
    val socName = "facebook"

    def apply(credentials: OAuthCredentials): Future[OAuthResult] = {
      ws.url("https://graph.facebook.com/v2.9/me")
          .withQueryStringParameters("fields" -> "email", "access_token" -> credentials.token).get()
          .map { response =>
            val jsResponse = response.json
            val userNetworkId = (jsResponse \ "id").asOpt[String].map(_.toLong)
            val email = (jsResponse \ "email").asOpt[String]

            userNetworkId
                .filter(uId => uId == credentials.userNetworkId)
                .map(uId => OAuthSuccess(uId, credentials.token, email))
                .getOrElse(OAuthFailed)
          }
    }
  }
}

class OAuthEmptyEmailException extends Exception
class OAuthNonEqualEmailException extends Exception
class OAuthNetworkNotFoundException extends Exception
class OAuthNetworkNotAllowedByUserException extends Exception
