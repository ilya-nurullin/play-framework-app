package actions

import javax.inject._

import errorJsonBodies.JsonErrors
import models.ApiAppDAO
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AppIdFilterAction @Inject() (apiAppDAO: ApiAppDAO) extends ActionBuilder[RequestWithAppIdAndKey] {

  def invokeBlock[A](request: Request[A], block: (RequestWithAppIdAndKey[A]) ⇒ Future[Result]): Future[Result] = {
    request.headers.get(Actions.AppKeyHeaderName).map { appKey =>
      apiAppDAO.getApp(appKey).flatMap {
        case Some(app) =>
          if (!app.isBanned)
            block(new RequestWithAppIdAndKey[A](app.id, appKey, request))
          else
            Future.successful(Results.Unauthorized(JsonErrors.AppKeyIsBanned))
        case None => Future.successful(Results.Unauthorized(JsonErrors.AppKeyIsNotFound))
      }
    } getOrElse Future.successful(Results.Unauthorized(JsonErrors.NeedAppKeyHeader))
  }
  
}

