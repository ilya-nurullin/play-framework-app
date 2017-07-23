package controllers

import errorJsonBodies.JsonErrors
import models.UsersApiTokenDAO
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import test.{BaseSpec, FutureTest}
import org.scalatest.time.SpanSugar._

import scala.language.postfixOps

trait AuthActionBehaviors extends BaseSpec with FutureTest {


  def authAction(act: => Action[AnyContent]) = {
    "be under AuthAction" should {
      "NOT Response 401 or 403 if App-key and Access-token are ok" in {
        val getMethod = act.apply(fakeRequestWithRightAuthHeaders)
        status(getMethod) must (not be FORBIDDEN or not be UNAUTHORIZED)
      }

      "Update token lifetime on query" in {
        val usersApiDAO = app.injector.instanceOf[UsersApiTokenDAO]
        Thread.sleep(1000) // to be sure that time will be updated
        whenReady(usersApiDAO.getToken(rightAccessToken), timeout(1 second)) { tokenOpt =>
          tokenOpt.isDefined mustBe true
          val expiresAt = tokenOpt.get.expiresAt

          whenReady(act.apply(fakeRequestWithRightAuthHeaders), timeout(1 second)) { _ =>
            whenReady(usersApiDAO.getToken(rightAccessToken), timeout(1 second)) { newTokenOpt =>
              newTokenOpt.isDefined mustBe true
              tokenOpt.get.eq(newTokenOpt.get) mustBe false

              assume(newTokenOpt.get.expiresAt.getMillis > expiresAt.getMillis)
              newTokenOpt.get.expiresAt.getMillis must be > expiresAt.getMillis
            }
          }

        }
      }

      "Response 403 FORBIDDEN if App-key !~> Access-key" in {
        val getMethod = act.apply(FakeRequest()
          .withHeaders("App-key" -> "2", "Access-token" -> rightAccessToken))

        status(getMethod) mustBe UNAUTHORIZED
        contentType(getMethod) mustBe Some("application/json")
        contentAsJson(getMethod) mustBe JsonErrors.AccessTokenIsNotValid
      }

      "Response 403 FORBIDDEN if Access-token is not valid" in {
        val getMethod = act.apply(fakeRequestWith_RightAppKey_WrongAccessToken)

        status(getMethod) mustBe UNAUTHORIZED
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

  def filterOnlyObjectOwnerAllowed(allowAction: => Action[AnyContent], denyAction: => Action[AnyContent],
                                   denyActionBody: Option[JsValue] = None) = {
    "Allow access only for an object owner" should {
      "Allow access for object owner" in {
        val updateMethod = allowAction.apply(fakeRequestWithRightAuthHeaders)

        status(updateMethod) must not be FORBIDDEN
      }
      "Deny access for other accounts" in {
        val updateMethod = denyAction.apply {
          if (denyActionBody.isEmpty)
            fakeRequestWithRightAuthHeaders
          else
            fakeRequestWithRightAuthHeaders.withJsonBody(denyActionBody.get)
        }

        status(updateMethod) mustBe FORBIDDEN
        contentAsJson(updateMethod) must matchPattern {
          case JsonErrors.ChangingSomeoneElsesObject =>
          case JsonErrors.GettingSomeoneElsesObject =>
          case JsonErrors.DeletingSomeoneElsesObject =>
        }
      }
    }
  }
}
