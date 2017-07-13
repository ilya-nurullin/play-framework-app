package helpers

import errorJsonBodies.JsonErrors
import play.api.data.Form
import play.api.mvc.{Request, Result, Results}

import scala.concurrent.Future

object JsonFormHelper extends Results {
  def asyncJsonForm[A](jsonForm: Form[A])(action: A => Future[Result])(implicit request: Request[_]): Future[Result] = {
    jsonForm.bindFromRequest.fold(
      hasError => Future.successful(BadRequest(JsonErrors.FormWithErrorsToJson(hasError))),
      json => action(json)
    )
  }

  def jsonForm[A](jsonForm: Form[A])(action: A => Result)(implicit request: Request[_]): Result = {
    jsonForm.bindFromRequest.fold(
      hasError => BadRequest(JsonErrors.FormWithErrorsToJson(hasError)),
      json => action(json)
    )
  }
}
