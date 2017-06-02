package controllers

import javax.inject._

import actions.Actions
import errorJsonBodies.JsonErrors
import models.{UserDAO, UsersApiTokenDAO}
import play.api.libs.json._
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserController @Inject() (userDAO: UserDAO, actions: Actions, mailerClient: MailerClient, usersApiTokenDAO: UsersApiTokenDAO) extends Controller {
  val AuthAction = actions.AuthAction
  implicit val fullUserFormat = Json.format[FullUserJson]

  case class FullUserJson(id: Int, login: String, name: Option[String], avatar: Option[String], cityId: Option[Int], userRankId: Int,
                          isBanned: Boolean)

  def get(id: Int) = AuthAction.async {
    userDAO.getById(id).map { userOpt =>
      userOpt.map { user =>
        val jsonUser = Json.toJson(FullUserJson(user.id, user.login, user.name, user.avatar, user.cityId,
          user.userRankId, user.isBanned))

        Ok(jsonUser)
      } getOrElse Ok(JsNull)
    }
  }

  def create() = actions.AppIdFilterAction.async { request =>
    request.body.asJson.map { js =>
      val emailOpt = (js \ "email").get.asOpt[String]
      val passwordOpt = (js \ "password").get.asOpt[String]

      if (emailOpt.isEmpty || passwordOpt.isEmpty)
        Future.successful(BadRequest(JsonErrors.EmailOrPasswordNotFound))
      else {
        import org.mindrot.jbcrypt.BCrypt

        userDAO.create(emailOpt.get, BCrypt.hashpw(passwordOpt.get, BCrypt.gensalt())).flatMap { userId =>
          mailerClient.send(Email("Поздравляем Вас с регистрацией", "Whipcake <noreply@whipcake.com>", Seq(emailOpt.get),
            Some("Поздравляем Вас с успешной регистрацией в Whipcake!")))
          usersApiTokenDAO.generateToken(request.appId, userId).map {
            case (token, expiresAt) =>
              Ok(Json.obj("token" -> token, "expiresAt" -> expiresAt)).withHeaders("Location" -> routes.UserController.get(userId).url)
            case _ => Ok("")
          }
        }.recover { case _: java.sql.SQLIntegrityConstraintViolationException => Conflict(JsonErrors.EmailAlreadySignedUp)}
      }
    } getOrElse Future.successful(BadRequest(JsonErrors.CannotCreateUser))
  }

  /*def update(userId: Int) = AuthAction.async { request =>
    if (request.userId != userId)
      return Future.successful(Forbidden("Only your account can be updated"))
    else {

    }
  }*/
}
