package controllers

import javax.inject._

import actions.Actions
import helpers.JsonFormHelper
import models.UsersApiTokenDAO
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class FirebaseController @Inject() (actions: Actions, usersApiTokenDAO: UsersApiTokenDAO)(implicit ec: ExecutionContext)
    extends InjectedController{
  def updateFirebaseUsersToken() = actions.AuthAction.async { implicit request =>
    val tokenForm = Form(single("firebaseToken" -> nonEmptyText))
    JsonFormHelper.asyncJsonForm(tokenForm) { token =>
      usersApiTokenDAO.updateFirebaseToken(request.userId, request.accessToken, token).map(_ => NoContent)
    }
  }
}
