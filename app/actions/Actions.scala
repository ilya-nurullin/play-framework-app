package actions

import javax.inject._

import errorJsonBodies.JsonErrors
import play.api.mvc.Result

import scala.concurrent.Future

object Actions {
  val AppKeyHeaderName = "App-key"
  val AccessTokenHeaderName = "Access-token"

  def filterOnlyObjectOwnerAllowed[A](currentUserId: Int)(block: => Future[Result])(implicit request: UserRequest[A]): Future[Result] = {
    import play.api.mvc.Results.Status
    import play.api.http.Status.FORBIDDEN

    if (request.userId == currentUserId)
      block
    else
      Future.successful(new Status(FORBIDDEN).apply(JsonErrors.ChangingSomeoneElsesObject))
  }
}

@Singleton
class Actions @Inject() (appIdFilterAction: AppIdFilterAction, accessTokenFilterAction: AccessTokenFilterAction) {
  val AuthAction = appIdFilterAction andThen accessTokenFilterAction
  val AppIdFilterAction = appIdFilterAction
}