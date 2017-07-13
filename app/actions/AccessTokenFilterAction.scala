package actions

import javax.inject._

import errorJsonBodies.JsonErrors
import models.{ApiAppDAO, UsersApiTokenDAO}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AccessTokenFilterAction @Inject() (usersApiToken: UsersApiTokenDAO, apiAppDAO: ApiAppDAO) extends ActionBuilder[UserRequest] {

  def invokeBlock[A](request: Request[A], block: (UserRequest[A]) => Future[Result]): Future[Result] = {
    request.headers.get(Actions.AccessTokenHeaderName).flatMap { token =>
      request.headers.get(Actions.AppKeyHeaderName).map { appKey =>
        usersApiToken.getToken(token).flatMap {
          case Some(usersToken) =>
            request match {
              case req: RequestWithAppIdAndKey[A] =>
                if (req.appId == usersToken.appId) {
                  usersApiToken.updateToken(usersToken.token)
                  block(new UserRequest[A](usersToken.userId, usersToken.appId, appKey, request))
                }
                else
                  Future.successful(Results.Forbidden(JsonErrors.AccessTokenIsNotValid))
              case _ => throw new ClassCastException("AccessTokenFilterAction received no RequestWithAppIdAndKey request")
            }
          case None => Future.successful(Results.Forbidden(JsonErrors.AccessTokenIsNotValid))
         }
      }
    } getOrElse Future.successful(Results.Unauthorized(JsonErrors.NeedAccessTokenHeader))
  }

}
