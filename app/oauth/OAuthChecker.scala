package oauth

import javax.inject._

import models.{UserDAO, UsersApiTokenDAO}
import play.api.libs.ws._

import scala.concurrent.{ExecutionContext, Future}

case class OAuthCredentials(userNetworkId: String, token: String)

abstract class OAuthResult
object OAuthFailed extends OAuthResult
case class OAuthSuccess(userNetworkId: String, token: String, email: Option[String]) extends OAuthResult

@Singleton
class OAuthChecker @Inject()(ws: WSClient, userDAO: UserDAO, usersApiTokenDAO: UsersApiTokenDAO)(implicit val ex: ExecutionContext) {

  def checkUserNetworkIdAndToken(networkName: String, credentials: OAuthCredentials) = {
    networkName match {
      case Facebook.socName => Facebook(credentials)
      case Google.socName => Google(credentials)
      case _ => Future.failed(new OAuthNetworkNotFoundException)
    }
  }


  private object Facebook {
    val socName = "facebook"
    val appId = "1934144683466884"

    def apply(credentials: OAuthCredentials): Future[OAuthResult] = {
      ws.url("https://graph.facebook.com/debug_token").withQueryStringParameters("input_token" -> credentials.token,
        "access_token" -> "***REMOVED***").get().flatMap { appCheckResponse =>

        val jsResponse = appCheckResponse.json
        val appIdOpt = (jsResponse \ "data" \ "app_id").asOpt[String].filter(_ == appId)
        val isTokenValidOpt = (jsResponse \ "data" \ "is_valid").asOpt[Boolean].filter(_ == true)

        if (appIdOpt.isEmpty || isTokenValidOpt.isEmpty)
          Future.successful(OAuthFailed)
        else
          ws.url("https://graph.facebook.com/v2.10/me")
            .withQueryStringParameters("fields" -> "email", "access_token" -> credentials.token).get()
            .map { response =>
              val jsResponse = response.json
              val userNetworkId = (jsResponse \ "id").asOpt[String]
              val email = (jsResponse \ "email").asOpt[String]

              userNetworkId
                  .filter(uId => uId == credentials.userNetworkId)
                  .map(uId => OAuthSuccess(uId, credentials.token, email))
                  .getOrElse(OAuthFailed)
            }
      }
    }
  }

  private object Google {
    val socName = "google"
    val appIds = Vector(
      "***REMOVED***", // Web
      "***REMOVED***", // Android
      "***REMOVED***" // iOS
    )

    def apply(credentials: OAuthCredentials): Future[OAuthResult] = {
      ws.url("https://www.googleapis.com/oauth2/v3/tokeninfo")
          .withQueryStringParameters("access_token" -> credentials.token).get().map { response =>
        val jsResponse = response.json
        val appIdOpt = (jsResponse \ "aud").asOpt[String]
        val userIdOpt = (jsResponse \ "sub").asOpt[String]
        val emailOpt = (jsResponse \ "email").asOpt[String]

        (for {
          appId <- appIdOpt if appIds.contains(appId)
          userId <- userIdOpt if credentials.userNetworkId == userId
        } yield OAuthSuccess(userId, credentials.token, emailOpt))
            .getOrElse(OAuthFailed)
      }
    }
  }
}

class OAuthEmptyEmailException extends Exception
class OAuthNetworkNotFoundException extends Exception

