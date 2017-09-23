package actions

import javax.inject._

import errorJsonBodies.JsonErrors
import models.{ApiAppDAO, UsersApiTokenDAO}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccessTokenFilterAction @Inject() (usersApiToken: UsersApiTokenDAO, apiAppDAO: ApiAppDAO, val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[UserRequest, AnyContent] {

  def invokeBlock[A](request: Request[A], block: (UserRequest[A]) => Future[Result]): Future[Result] = {
    request.headers.get(Actions.AccessTokenHeaderName).flatMap { token =>
      request.headers.get(Actions.AppKeyHeaderName).map { appKey =>
        usersApiToken.getToken(token).flatMap {
          case Some(usersToken) =>
            request match {
              case req: RequestWithAppIdAndKey[A] =>
                if (req.appId == usersToken.appId) {
                  usersApiToken.updateToken(usersToken.token)
                  block(new UserRequest[A](usersToken.userId, usersToken.appId, appKey, usersToken.token, request))
                }
                else
                  Future.successful(Results.Unauthorized(JsonErrors.AccessTokenIsNotValid))
              case _ => throw new ClassCastException("AccessTokenFilterAction received no RequestWithAppIdAndKey request")
            }
          case None => Future.successful(Results.Unauthorized(JsonErrors.AccessTokenIsNotValid))
         }
      }
    } getOrElse Future.successful(Results.Unauthorized(JsonErrors.NeedAccessTokenHeader))
  }

}
