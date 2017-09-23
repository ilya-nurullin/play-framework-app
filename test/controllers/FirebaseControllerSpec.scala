package controllers

import models.UsersApiTokenDAO
import play.api.libs.json.Json
import play.api.test.Helpers._
import test.{BaseSpec, FutureTest}

class FirebaseControllerSpec extends BaseSpec with FutureTest {
  "Update firebase token" should {
    "works fine" in {
      val newFirebaseToken = "newFirebaseToken"
      val jsonBody = Json.obj("firebaseToken" -> newFirebaseToken)
      val method = app.injector.instanceOf[FirebaseController].updateFirebaseUsersToken()
          .apply(fakeRequestWithRightAuthHeaders withJsonBody jsonBody)

      status(method) mustBe NO_CONTENT

      whenReady(app.injector.instanceOf[UsersApiTokenDAO].getToken(rightAccessToken)) { tokenOption =>
        tokenOption.isDefined mustBe true
        tokenOption.get.firebaseToken mustBe newFirebaseToken
      }
    }
  }
}
