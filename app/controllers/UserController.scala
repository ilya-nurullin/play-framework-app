package controllers

import javax.inject._

import actions.Actions
import errorJsonBodies.JsonErrors
import helpers.DateHelper
import models.{UserDAO, UsersApiTokenDAO}
import play.api.libs.json._
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.data._
import play.api.data.Forms._

@Singleton
class UserController @Inject() (userDAO: UserDAO, actions: Actions, mailerClient: MailerClient, usersApiTokenDAO: UsersApiTokenDAO) extends Controller {
  val AuthAction = actions.AuthAction
  implicit val fullUserFormat = Json.format[FullUserJson]

  def get(id: Int) = AuthAction.async {
    userDAO.getById(id).map { userOpt =>
      userOpt.map { user =>
        val jsonUser = Json.toJson(FullUserJson(user.id, user.login, user.name, user.avatar, user.cityId,
          user.userRankId, user.isBanned))

        Ok(jsonUser)
      } getOrElse NotFound(JsNull)
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

  def update(userId: Int) = AuthAction.async { implicit request =>
    import errorJsonBodies.JsonErrors

    Actions.filterOnlyObjectOwnerAllowed(userId) {

      val jsonUser = Form(
        mapping(
          "login" -> nonEmptyText,
          "email" -> email,
          "name" -> optional(nonEmptyText),
          "avatar" -> optional(nonEmptyText),
          "aboutMyself" -> optional(text),
          "dateOfBirth" -> optional(text),
          "sex" -> optional(boolean),
          "cityId" -> optional(number)
        )(UserToUpdate.apply)(UserToUpdate.unapply)
      )

      jsonUser.bindFromRequest.fold (
        formWithErrors => {
          val errors = formWithErrors.errors.foldLeft(Map[String, String]()) { (m, e) => m + (e.key -> e.message) }
          Future.successful(BadRequest(JsonErrors.BadData(Json.toJson(errors))))
        },
        userData => {
          userDAO.update(userId, userData.login, userData.email, userData.name, userData.avatar, userData.aboutMyself,
            userData.dateOfBirth.map(DateHelper.sqlDateStringToDate), userData.sex, userData.cityId).map { user =>

            val jsonUser = Json.toJson(FullUserJson(user.id, user.login, user.name, user.avatar, user.cityId,
              user.userRankId, user.isBanned))
            Ok(jsonUser)

          }.recover {
            //case e: java.sql.SQLIntegrityConstraintViolationException => // only for MySql, doesn't work for h2
            case e: Exception =>
              if (e.getMessage.toUpperCase.contains("LOGIN_UNIQUE"))
                BadRequest(JsonErrors.LoginDuplication)
              else if (e.getMessage.toUpperCase.contains("EMAIL_UNIQUE"))
                BadRequest(JsonErrors.EmailDuplication)
              else
                throw e
          }
        }
      )

    }
  }


  case class FullUserJson(
                           id: Int,
                           login: String,
                           name: Option[String],
                           avatar: Option[String],
                           cityId: Option[Int],
                           userRankId: Int,
                           isBanned: Boolean
                         )

  case class UserToUpdate(
                           login: String,
                           email: String,
                           name: Option[String],
                           avatar: Option[String],
                           aboutMyself: Option[String],
                           dateOfBirth: Option[String],
                           sex: Option[Boolean],
                           cityId: Option[Int]
                         )
}
