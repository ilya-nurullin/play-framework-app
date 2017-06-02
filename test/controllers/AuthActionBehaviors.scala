package controllers

import errorJsonBodies.JsonErrors
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import test.BaseSpec

trait AuthActionBehaviors extends BaseSpec {


  def authAction(act: => Action[AnyContent]) = {
    "be under AuthAction" should {
      "Response 200 OK if App-key and Access-token are ok" in {
        val getMethod = act.apply(fakeRequestWithRightAuthHeaders)
        status(getMethod) mustBe OK
      }

      "Response 403 FORBIDDEN if App-key !~> Access-key" in {
        val getMethod = act.apply(FakeRequest()
          .withHeaders("App-key" -> "2", "Access-token" -> rightAccessToken))

        status(getMethod) mustBe FORBIDDEN
        contentType(getMethod) mustBe Some("application/json")
        contentAsJson(getMethod) mustBe JsonErrors.AccessTokenIsNotValid
      }

      "Response 403 FORBIDDEN if Access-token is not valid" in {
        val getMethod = act.apply(fakeRequestWith_RightAppKey_WrongAccessToken)

        status(getMethod) mustBe FORBIDDEN
        contentType(getMethod) mustBe Some("application/json")
        contentAsJson(getMethod) mustBe JsonErrors.AccessTokenIsNotValid
      }

      "Response 401 UNAUTHORIZED if App-key is not found" in {
        val getMethod = act.apply(fakeRequestWith_WrongAppKey_RightAccessToken)

        status(getMethod) mustBe UNAUTHORIZED
        contentType(getMethod) mustBe Some("application/json")
        contentAsJson(getMethod) mustBe JsonErrors.AppKeyIsNotFound
      }

      "Response 401 UNAUTHORIZED if App-key is banned" in {
        val getMethod = act.apply(FakeRequest()
          .withHeaders("App-key" -> "banned", "Access-token" -> rightAccessToken))

        status(getMethod) mustBe UNAUTHORIZED
        contentType(getMethod) mustBe Some("application/json")
        contentAsJson(getMethod) mustBe JsonErrors.AppKeyIsBanned
      }
    }
  }
}
