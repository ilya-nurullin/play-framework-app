package controllers

import models.UserDAO
import play.api.libs.json.{JsNull, Json}
import play.api.test.Helpers._
import test.{BaseSpec, FutureTest}

class UserControllerSpec extends BaseSpec with AuthActionBehaviors with FutureTest {
  "User controller GET" should {
    val controller = app.injector.instanceOf[UserController]

    behave like authAction(controller.get(1))

    "Response 200 OK with JSON object of right user" in {
      val getMethod = controller.get(1).apply(fakeRequestWithRightAuthHeaders)
      status(getMethod) mustBe OK
      contentType(getMethod) mustBe Some("application/json")
      val json = contentAsJson(getMethod)

      (json \ "id").as[Int] mustBe 1
      (json \ "login").as[String] mustBe "testLogin"
    }

    "Response 200 OK with JSON object of user without passHash" in {
      val getMethod = controller.get(1).apply(fakeRequestWithRightAuthHeaders)
      status(getMethod) mustBe OK
      contentType(getMethod) mustBe Some("application/json")
      val json = contentAsJson(getMethod)

      (json \ "passHash").asOpt[String] mustBe None
    }

    "Response 200 OK null if user is not found" in {
      val getMethod = controller.get(100).apply(fakeRequestWithRightAuthHeaders)

      status(getMethod) mustBe OK
      contentType(getMethod) mustBe Some("application/json")
      contentAsJson(getMethod) mustBe JsNull
    }
  }

  "User controller POST" should {
    val controller = app.injector.instanceOf[UserController]

    "Check App-id header" in {
      val methodPost = controller.create().apply(fakeRequestWithRightAppKeyHeader.withJsonBody(
        Json.obj("email" -> "e", "password" -> "somePassword")
      ))

      val badMethodPost = controller.create().apply(fakeRequestWithWrongAppKeyHeader.withJsonBody(
        Json.obj("email" -> "e1", "password" -> "somePassword")
      ))
      status(methodPost) mustBe OK
      status(badMethodPost) mustBe UNAUTHORIZED
    }

    "Create a new user" in {
      val methodPost = controller.create().apply(fakeRequestWithRightAppKeyHeader.withJsonBody(
        Json.obj("email" -> "someEmail", "password" -> "somePassword")
      ))

      status(methodPost) mustBe OK

      val userDAO = app.injector.instanceOf[UserDAO]

      whenReady(userDAO.getByEmail("someEmail")) { userOpt =>
        userOpt.isDefined mustBe true
        val user = userOpt.get
        header("Location", methodPost) mustBe Some(routes.UserController.get(user.id).toString)
        (contentAsJson(methodPost) \ "token").asOpt[String].isDefined mustBe true
      }
    }
  }
}