package actions

import javax.inject._

import errorJsonBodies.JsonErrors
import play.api.mvc.{Result, Results}

import scala.concurrent.Future

object Actions extends Results {
  val AppKeyHeaderName = "App-key"
  val AccessTokenHeaderName = "Access-token"

  def filterOnlyObjectOwnerAllowed[A](currentUserId: Int)(block: => Future[Result])(implicit request: UserRequest[A]): Future[Result] = {
    if (request.userId == currentUserId)
      block
    else
      Future.successful(Forbidden(JsonErrors.ChangingSomeoneElsesObject))
  }
}

@Singleton
class Actions @Inject() (appIdFilterAction: AppIdFilterAction, accessTokenFilterAction: AccessTokenFilterAction) {
  val AuthAction = appIdFilterAction andThen accessTokenFilterAction
  val AppIdFilterAction = appIdFilterAction
}