package controllers

import javax.inject._

import actions.Actions
import errorJsonBodies.JsonErrors
import helpers.{DateHelper, JsonFormHelper}
import models.{ProjectDAO, TaskDAO, UserDAO, UsersApiTokenDAO}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json._
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc._
import org.mindrot.jbcrypt.BCrypt
import play.api.i18n.{I18nSupport, Messages}

import scala.concurrent.{ExecutionContext, Future}
import json.implicits.formats.DateJsonFormat._
import org.joda.time.DateTime

@Singleton
class UserController @Inject()(userDAO: UserDAO, actions: Actions, mailerClient: MailerClient,
                                usersApiTokenDAO: UsersApiTokenDAO, projectDAO: ProjectDAO, taskDAO: TaskDAO)
                              (implicit ec: ExecutionContext)
    extends InjectedController with I18nSupport {

  val AuthAction = actions.AuthAction
  implicit val fullUserFormat = Json.format[FullUserJson]

  def get(id: Int) = AuthAction.async {
    userDAO.getById(id).map { userOpt =>
      userOpt.map { user =>
        val jsonUser = Json.toJson(FullUserJson(user.id, user.login, user.name, user.avatar, user.aboutMyself, user.sex,
          user.cityId, user.statuses, user.userRankId, user.premiumUntil, user.isBanned, user.defaultProject))

        Ok(jsonUser)
      } getOrElse NotFound(JsNull)
    }
  }

  def create() = actions.AppIdFilterAction.async { implicit request =>
    val jsonRequest = Form(
      tuple(
        "email" -> email,
        "password" -> nonEmptyText(6),
        "firebaseToken" -> nonEmptyText
      )
    )

    JsonFormHelper.asyncJsonForm(jsonRequest) {
      case (email, password, firebaseToken) =>
        userDAO.create(email, BCrypt.hashpw(password, BCrypt.gensalt())).flatMap { userId =>
          mailerClient.send(Email(Messages("registration.email.title"), "Whipcake <noreply@whipcake.com>", Seq(email),
            Some(Messages("registration.email.body"))))
          usersApiTokenDAO.generateToken(request.appId, userId, firebaseToken).map {
            case (token, _) =>
              projectDAO.createDefaultProject(userId).map(taskDAO.createGreetingTasks(userId, _))
              Ok(Json.obj("token" -> token, "userId" -> userId)).withHeaders("Location" -> routes.UserController.get(userId).url)
            case _ => Ok(JsNull)
          }
        }.recover {
          case _: java.sql.SQLIntegrityConstraintViolationException => Conflict(JsonErrors.EmailAlreadySignedUp)
        }
    }

  }

  def update() = AuthAction.async { implicit request =>
    val userId: Int = request.userId

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

    jsonUser.bindFromRequest.fold(
      formWithErrors => {
        val errors = formWithErrors.errors.foldLeft(Map[String, String]()) { (m, e) => m + (e.key -> e.message) }
        Future.successful(BadRequest(JsonErrors.BadData(Json.toJson(errors))))
      },
      userData => {
        userDAO.update(userId, userData.login, userData.email, userData.name, userData.avatar, userData.aboutMyself,
          userData.dateOfBirth.map(DateHelper.sqlDateStringToDate), userData.sex, userData.cityId).map { user =>

          val jsonUser = Json.toJson(FullUserJson(user.id, user.login, user.name, user.avatar, user.aboutMyself, user.sex,
            user.cityId, user.statuses, user.userRankId, user.premiumUntil, user.isBanned, user.defaultProject))
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

  def changePassword() = actions.AuthAction.async { implicit request =>
    val userId: Int = request.userId
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

  def changeDefaultProject() = actions.AuthAction.async { implicit request =>
    val projectFrom = Form(
      single(
        "projectId" -> longNumber
      )
    )
    JsonFormHelper.asyncJsonForm(projectFrom) { projectId =>
      projectDAO.isProjectOwner(request.userId, projectId).flatMap { isOwner =>
        if (isOwner)
          for {
            _ <- userDAO.changeDefaultProject(request.userId, projectId)
          } yield NoContent
        else
          Future.successful(Forbidden(JsonErrors.ChangingSomeoneElsesObject))
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
                           statuses: Option[String],
                           userRankId: Int,
                           premiumUntil: Option[DateTime],
                           isBanned: Boolean,
                           defaultProject: Option[Long]
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
