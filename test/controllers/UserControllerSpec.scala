package controllers

import errorJsonBodies.JsonErrors
import models.{ProjectDAO, TaskDAO, UserDAO}
import play.api.libs.json.{JsNull, Json}
import play.api.test.Helpers._
import test.{BaseSpec, FutureTest}

class UserControllerSpec extends BaseSpec with AuthActionBehaviors with FutureTest {
  "User controller" should {
    "GET" should {
      val controller = app.injector.instanceOf[UserController]

      behave like authAction(controller.get(1))

      "Response 200 OK with JSON object of right user" in {
        val getMethod = controller.get(1).apply(fakeRequestWithRightAuthHeaders)
        status(getMethod) mustBe OK
        contentType(getMethod) mustBe Some("application/json")
        val json = contentAsJson(getMethod)

        (json \ "id").as[Int] mustBe 1
        (json \ "login").as[String] mustBe "testLogin"
        (json \ "name").as[String] mustBe "testName"
        (json \ "avatar").as[String] mustBe "testAva"
        (json \ "aboutMyself").as[String] mustBe "testAboutMyself"
        (json \ "sex").as[Boolean] mustBe false
        (json \ "statuses").as[String] mustBe "[\"testStatus\"]"
        (json \ "userRankId").as[Int] mustBe 1
        (json \ "premiumUntil").as[String] mustBe "2017-05-01T21:10:14+0000"
        (json \ "isBanned").as[Boolean] mustBe false
        (json \ "defaultProject").as[Int] mustBe 1
      }

      "Response 200 OK with JSON object of user without passHash" in {
        val getMethod = controller.get(1).apply(fakeRequestWithRightAuthHeaders)
        status(getMethod) mustBe OK
        contentType(getMethod) mustBe Some("application/json")
        val json = contentAsJson(getMethod)

        (json \ "passHash").asOpt[String] mustBe None
      }

      "Response 404 Not Found null if user is not found" in {
        val getMethod = controller.get(100).apply(fakeRequestWithRightAuthHeaders)

        status(getMethod) mustBe NOT_FOUND
        contentType(getMethod) mustBe Some("application/json")
        contentAsJson(getMethod) mustBe JsNull
      }

      "Get me" in {
        val method = controller.getMe.apply(fakeRequestWithRightAuthHeaders)
        status(method) mustBe OK
        (contentAsJson(method) \ "id").as[Int] mustBe 1
      }
    }

    "POST" should {
      val controller = app.injector.instanceOf[UserController]

      "Check App-id header" in {
        val methodPost = controller.create().apply(fakeRequestWithRightAppKeyHeader.withJsonBody(
          Json.obj("email" -> "e@e.e", "password" -> "somePassword", "firebaseToken" -> "someToken")
        ))

        val badMethodPost = controller.create().apply(fakeRequestWithWrongAppKeyHeader.withJsonBody(
          Json.obj("email" -> "e1", "password" -> "somePassword", "firebaseToken" -> "someToken")
        ))
        status(methodPost) mustBe OK
        status(badMethodPost) mustBe UNAUTHORIZED
      }

      "Create a new user with default tasks" in {
        val email = "someEmail@e.e"
        val methodPost = controller.create().apply(fakeRequestWithRightAppKeyHeader.withJsonBody(
          Json.obj("email" -> email, "password" -> "somePassword", "firebaseToken" -> "someToken")
        ))

        status(methodPost) mustBe OK

        val userDAO = app.injector.instanceOf[UserDAO]

        whenReady(userDAO.getByEmail(email)) { userOpt =>
          userOpt.isDefined mustBe true
          val user = userOpt.get
          header("Location", methodPost) mustBe Some(routes.UserController.get(user.id).toString)
          (contentAsJson(methodPost) \ "token").asOpt[String].isDefined mustBe true

          whenReady(app.injector.instanceOf[ProjectDAO].getLatestProjects(user.id)) { projects =>
            projects.length mustBe 1
          }

          whenReady(app.injector.instanceOf[TaskDAO].getLatestTasks(user.id)) { tasks =>
            tasks.length mustBe 3
          }
        }
      }
    }

    "PUT" should {
      val controller = app.injector.instanceOf[UserController]

      behave like authAction(controller.update())

      "Change all user fields in DB and response updated user" in {
        val newEmail = "newemail@testdomain.test"
        val newLogin = "newLogin"
        val newName = "newName"
        val newAvatar = "newAvatar"
        val newAboutMyself = "newAboutMyself"
        val newDateOfBirth = "2017-04-01"
        val newSex = true
        val newCityId = 1

        val updateMethod = controller.update().apply(fakeRequestWithRightAuthHeaders.withJsonBody(Json.obj(
          "email" -> newEmail,
          "login" -> newLogin,
          "name" -> newName,
          "avatar" -> newAvatar,
          "aboutMyself" -> newAboutMyself,
          "dateOfBirth" -> newDateOfBirth,
          "sex" -> newSex,
          "cityId" -> newCityId
        )))

        status(updateMethod) mustBe OK
        val jsonResponse = contentAsJson(updateMethod)
        (jsonResponse \ "login").as[String] mustBe newLogin
        (jsonResponse \ "name").as[String] mustBe newName
        (jsonResponse \ "avatar").as[String] mustBe newAvatar
        (jsonResponse \ "cityId").as[Int] mustBe newCityId

        whenReady(app.injector.instanceOf[UserDAO].getById(1)) { userOpt =>
          userOpt.isDefined mustBe true
          val user = userOpt.get
          user.email mustBe newEmail
          user.login mustBe newLogin
          user.name.get mustBe newName
          user.avatar.get mustBe newAvatar
          user.aboutMyself.get mustBe newAboutMyself
          user.dateOfBirth.get.toString mustBe newDateOfBirth
          user.sex.get mustBe newSex
          user.cityId.get mustBe newCityId
        }
      }

      "Show error on error in email" in {
        val updateMethodEmailError = controller.update().apply(fakeRequestWithRightAuthHeaders.withJsonBody(Json.obj(
          "email" -> "newEmail",
          "login" -> "newLogin"
        )))

        status(updateMethodEmailError) mustBe BAD_REQUEST
        val jsonResponse = contentAsJson(updateMethodEmailError)
        (jsonResponse \ "error").as[String] mustBe "bad_data"
        (jsonResponse \ "data" \ "email").as[String] mustBe "error.email"
      }

      "Show error on unique fields duplication" in {
        import errorJsonBodies.JsonErrors
        val updateMethodEmailDuplication = controller.update().apply(fakeRequestWithRightAuthHeaders.withJsonBody(Json.obj(
          "email" -> "testemail@testdomain.test",
          "login" -> "newLogin"
        )))

        val updateMethodLoginDuplication = controller.update().apply(fakeRequestWithRightAuthHeaders.withJsonBody(Json.obj(
          "email" -> "newemail@testdomain.test",
          "login" -> "id2"
        )))

        status(updateMethodEmailDuplication) mustBe BAD_REQUEST
        status(updateMethodLoginDuplication) mustBe BAD_REQUEST

        contentAsJson(updateMethodEmailDuplication) mustBe JsonErrors.EmailDuplication
        contentAsJson(updateMethodLoginDuplication) mustBe JsonErrors.LoginDuplication
      }
    }

    "Change password" should {
      val controller = app.injector.instanceOf[UserController]

      behave like authAction(controller.changePassword())

      "Change password" in {
        val jsonRequest = Json.obj(
          "oldPassword" -> "testPassword",
          "newPassword" -> "newPass"
        )
        val changeMethod = controller.changePassword().apply(fakeRequestWithRightAuthHeaders.withJsonBody(jsonRequest))

        status(changeMethod) mustBe NO_CONTENT

        whenReady(app.injector.instanceOf[UserDAO].getById(1)) { userOpt =>
          userOpt.isDefined mustBe true
          val user = userOpt.get

          import org.mindrot.jbcrypt.BCrypt

          BCrypt.checkpw("newPass", user.passHash) mustBe true
        }
      }

      "Deny change password if old password is wrong" in {
        val jsonRequest = Json.obj(
          "oldPassword" -> "testPassword",
          "newPassword" -> "newPass"
        )
        val changeMethod = controller.changePassword().apply(fakeRequestWithRightAuthHeaders.withJsonBody(jsonRequest))

        status(changeMethod) mustBe FORBIDDEN
      }
    }

    "Change default project" should {
      val controller = app.injector.instanceOf[UserController]

      "BE OK" in {
        val jsonBody = Json.obj("projectId" -> 3)
        val method = controller.changeDefaultProject().apply(fakeRequestWithRightAuthHeaders.withJsonBody(jsonBody))

        status(method) mustBe NO_CONTENT

        whenReady(app.injector.instanceOf[UserDAO].getById(1)) { userOpt =>
          val user = userOpt.get
          user.defaultProject.get mustBe 3
        }
      }

      "Deny on changing on non own project" in {
        val jsonBody = Json.obj("projectId" -> 2)
        val method = controller.changeDefaultProject().apply(fakeRequestWithRightAuthHeaders.withJsonBody(jsonBody))

        status(method) mustBe FORBIDDEN
        contentAsJson(method) mustBe JsonErrors.ChangingSomeoneElsesObject
      }
    }
  }
}