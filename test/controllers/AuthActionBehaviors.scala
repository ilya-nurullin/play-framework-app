package controllers

import errorJsonBodies.JsonErrors
import models.UsersApiTokenDAO
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import test.{BaseSpec, FutureTest}

trait AuthActionBehaviors extends BaseSpec with FutureTest {


  def authAction(act: => Action[AnyContent]) = {
    "be under AuthAction" should {
      "Response 200 OK if App-key and Access-token are ok" in {
        val getMethod = act.apply(fakeRequestWithRightAuthHeaders)
        status(getMethod) mustBe OK
      }

      "Update token lifetime on query" in {
        val usersApiDAO = app.injector.instanceOf[UsersApiTokenDAO]

        whenReady(usersApiDAO.getToken(rightAccessToken)) { tokenOpt =>
          tokenOpt.isDefined mustBe true
          val expiresAt = tokenOpt.get.expiresAt
          act.apply(fakeRequestWithRightAuthHeaders)

          Thread.sleep(500) // to be sure that time has changed

          whenReady(usersApiDAO.getToken(rightAccessToken)) { newTokenOpt =>
            newTokenOpt.isDefined mustBe true
            tokenOpt.get.eq(newTokenOpt.get) mustBe false
            newTokenOpt.get.expiresAt.getMillis must be > expiresAt.getMillis
          }
        }
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

  def filterOnlyObjectOwnerAllowed(allowAction: => Action[AnyContent], denyAction: => Action[AnyContent]) = {
    "Allow access only for an object owner" should {
      "Allow access for object owner" in {
        val updateMethod = allowAction.apply(fakeRequestWithRightAuthHeaders)

        status(updateMethod) mustBe OK
      }
      "Deny access for other accounts" in {
        val updateMethod = denyAction.apply(fakeRequestWithRightAuthHeaders)

        status(updateMethod) mustBe FORBIDDEN
        contentAsJson(updateMethod) mustBe JsonErrors.ChangingSomeoneElsesObject
      }
    }
  }
}
