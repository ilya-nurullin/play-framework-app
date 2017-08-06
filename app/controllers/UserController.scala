package controllers

import javax.inject._

import actions.Actions
import errorJsonBodies.JsonErrors
import helpers.{DateHelper, JsonFormHelper}
import models.{UserDAO, UsersApiTokenDAO}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json._
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc._
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.{ExecutionContext, Future}
import json.implicits.formats.DateJsonFormat._

@Singleton
class UserController @Inject() (userDAO: UserDAO, actions: Actions, mailerClient: MailerClient,
                                usersApiTokenDAO: UsersApiTokenDAO)(implicit ec: ExecutionContext) extends InjectedController {
  val AuthAction = actions.AuthAction
  implicit val fullUserFormat = Json.format[FullUserJson]

  def get(id: Int) = AuthAction.async {
    userDAO.getById(id).map { userOpt =>
      userOpt.map { user =>
        val jsonUser = Json.toJson(FullUserJson(user.id, user.login, user.name, user.avatar, user.aboutMyself, user.sex,
          user.cityId, user.userRankId, user.isBanned))

        Ok(jsonUser)
      } getOrElse NotFound(JsNull)
    }
  }

  def create() = actions.AppIdFilterAction.async { implicit request =>
    val jsonRequest = Form(
      tuple(
        "email" -> email,
        "password" -> nonEmptyText(6)
      )
    )

    JsonFormHelper.asyncJsonForm(jsonRequest) { emailPassword =>
      val email = emailPassword._1
      val password = emailPassword._2

      userDAO.create(email, BCrypt.hashpw(password, BCrypt.gensalt())).flatMap { userId =>
        mailerClient.send(Email("Поздравляем Вас с регистрацией", "Whipcake <noreply@whipcake.com>", Seq(email),
          Some("Поздравляем Вас с успешной регистрацией в Whipcake!")))
        usersApiTokenDAO.generateToken(request.appId, userId).map {
          case (token, _) =>
            Ok(Json.obj("token" -> token)).withHeaders("Location" -> routes.UserController.get(userId).url)
          case _ => Ok(JsNull)
        }
      }.recover {
        case _: java.sql.SQLIntegrityConstraintViolationException => Conflict(JsonErrors.EmailAlreadySignedUp)
      }
    }

  }

  def update(userId: Int) = AuthAction.async { implicit request =>
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

            val jsonUser = Json.toJson(FullUserJson(user.id, user.login, user.name, user.avatar, user.aboutMyself, user.sex,
              user.cityId, user.userRankId, user.isBanned))
            Ok(jsonUser)

          }.recover {
            case e: java.sql.SQLIntegrityConstraintViolationException =>
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

  def changePassword(userId: Int) = actions.AuthAction.async { implicit request =>
    Actions.filterOnlyObjectOwnerAllowed(userId) {
      val jsonRequest = Form(
        tuple(
          "oldPassword" -> nonEmptyText(6),
          "newPassword" -> nonEmptyText(6)
        )
      )

      JsonFormHelper.asyncJsonForm(jsonRequest) { passTuple =>
        userDAO.getById(userId).flatMap { userOpt =>
          val user = userOpt.get
          if (BCrypt.checkpw(passTuple._1, user.passHash)) {
            userDAO.changePassword(userId, BCrypt.hashpw(passTuple._2, BCrypt.gensalt())).map { _ =>
              NoContent
            }
          }
          else
            Future.successful(Forbidden(JsonErrors.WrongPassword))
        }
      }
    }
  }


  case class FullUserJson(
                           id: Int,
                           login: String,
                           name: Option[String],
                           avatar: Option[String],
                           aboutMyself: Option[String],
                           sex: Option[Boolean],
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
