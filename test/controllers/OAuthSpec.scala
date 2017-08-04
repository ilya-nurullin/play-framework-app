package controllers

import errorJsonBodies.JsonErrors
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import test.{BaseSpec, FutureTest}

class OAuthSpec extends BaseSpec with FutureTest {
  "Facebook OAuth" should {
    val controller = app.injector.instanceOf[OAuthController]
    val facebookToken = "EAAbfGCbNYIQBAKe1eRtz9MgNNRjbQRI1cxZB8yEA2ehh2ZBR9po7dvTgLCo9h2TlBCgJ2w773b5NvvV5GvQgvua1YYBr3dZBfaHPmaNXbFQ3mL4OACDoYJSAZCe338YfxUGphCGTkvEpfMRmv8zJdpSyzhrgsEUZA139XF8blOg8udE15UgZBo"
    val email = "***REMOVED***"
    val userNetworkId: Long = ***REMOVED***L

    "Registration" should {
      "Allow access for right App-key header" in {
        val registrationMethod = controller.registration("facebook").apply(fakeRequestWithRightAppKeyHeader)
        status(registrationMethod) must not be FORBIDDEN
      }

      "Deny access for wrong App-key header" in {
        val registrationMethod = controller.registration("facebook").apply(fakeRequestWithWrongAppKeyHeader)
        status(registrationMethod) mustBe UNAUTHORIZED
        contentAsJson(registrationMethod) mustBe JsonErrors.AppKeyIsNotFound
      }

      "Register new user" in {
        val jsRequest = Json.obj(
          "token" -> facebookToken,
          "userNetworkId" -> userNetworkId
        )

        val registrationMethod = controller.registration("facebook").apply(fakeRequestWithRightAppKeyHeader.withJsonBody(jsRequest))

        status(registrationMethod) mustBe OK
        val jsResponse = contentAsJson(registrationMethod)
        (jsResponse \ "token").asOpt[String].isDefined mustBe true
      }

      "Wrong userNetworkId" in {
        val jsRequest = Json.obj(
          "token" -> facebookToken,
          "userNetworkId" -> (userNetworkId-1)
        )

        val registrationMethod = controller.registration("facebook").apply(fakeRequestWithRightAppKeyHeader.withJsonBody(jsRequest))

        status(registrationMethod) mustBe BAD_REQUEST
        val jsResponse = contentAsJson(registrationMethod)
        jsResponse mustBe JsonErrors.OAuthFailed
      }

      "Wrong token" in {
        val jsRequest = Json.obj(
          "token" -> "ASDASDASD",
          "userNetworkId" -> userNetworkId
        )

        val registrationMethod = controller.registration("facebook").apply(fakeRequestWithRightAppKeyHeader.withJsonBody(jsRequest))

        status(registrationMethod) mustBe BAD_REQUEST
        val jsResponse = contentAsJson(registrationMethod)
        jsResponse mustBe JsonErrors.OAuthFailed
      }
    }

    "Auth" should {
      "Allow access for right App-key header" in {
        val registrationMethod = controller.auth("facebook").apply(fakeRequestWithRightAppKeyHeader)
        status(registrationMethod) must not be FORBIDDEN
      }

      "Deny access for wrong App-key header" in {
        val registrationMethod = controller.auth("facebook").apply(fakeRequestWithWrongAppKeyHeader)
        status(registrationMethod) mustBe UNAUTHORIZED
        contentAsJson(registrationMethod) mustBe JsonErrors.AppKeyIsNotFound
      }

      "Auth if user already used oauth" in {
        val jsRequest = Json.obj(
          "token" -> facebookToken,
          "userNetworkId" -> userNetworkId
        )

        val registrationMethod = controller.auth("facebook").apply(FakeRequest().withHeaders("App-key" -> "2").withJsonBody(jsRequest))
        status(registrationMethod) mustBe OK
        val jsResponse = contentAsJson(registrationMethod)
        (jsResponse \ "token").asOpt[String].isDefined mustBe true
      }

      "Wrong userNetworkId" in {
        val jsRequest = Json.obj(
          "token" -> facebookToken,
          "userNetworkId" -> (userNetworkId-1)
        )

        val registrationMethod = controller.auth("facebook").apply(fakeRequestWithRightAppKeyHeader.withJsonBody(jsRequest))

        status(registrationMethod) mustBe BAD_REQUEST
        val jsResponse = contentAsJson(registrationMethod)
        jsResponse mustBe JsonErrors.OAuthFailed
      }

      "Wrong token" in {
        val jsRequest = Json.obj(
          "token" -> "ASDASDASD",
          "userNetworkId" -> userNetworkId
        )

        val registrationMethod = controller.auth("facebook").apply(fakeRequestWithRightAppKeyHeader.withJsonBody(jsRequest))

        status(registrationMethod) mustBe BAD_REQUEST
        val jsResponse = contentAsJson(registrationMethod)
        jsResponse mustBe JsonErrors.OAuthFailed
      }


      "Add social network for user" in {
        whenReady(app.injector.instanceOf[models.UserDAO].getByEmail(email)) { userOpt =>
          whenReady(app.injector.instanceOf[models.UserHasSocialNetworkDAO].isSocialNetworkAllowedByUser(userOpt.get.id, "facebook")) { bool =>
            bool mustBe true
          }
        }
      }
    }
  }
}
