package controllers

import models.UsersApiTokenDAO
import org.scalatest.concurrent.{Futures, ScalaFutures}
import play.api.test._
import play.api.test.Helpers._
import test.{BaseSpec, FutureTest}

class AuthControllerSpec extends BaseSpec with FutureTest {
  "Auth controller" should {
    "POST" should {
      val controller = app.injector.instanceOf[AuthController]

      "Deny access without App-key header" in {
        val authMethod = controller.emailAuth().apply(FakeRequest())

        status(authMethod) mustBe UNAUTHORIZED
        contentType(authMethod) mustBe Some("application/json")
        contentAsString(authMethod) must include("{")
      }
    }

    "DELETE" should {
      val controller = app.injector.instanceOf[AuthController]

      "Logout action" in {
        val logoutMethod = controller.logout().apply(fakeRequestWithRightAuthHeaders)
        status(logoutMethod) mustBe NO_CONTENT
        contentAsString(logoutMethod) mustBe ""

        whenReady(app.injector.instanceOf[UsersApiTokenDAO].getToken(rightAccessToken)) { tokenOpt =>
          tokenOpt.isEmpty mustBe true
        }
      }
    }
  }
}
