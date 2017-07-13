package controllers

import errorJsonBodies.JsonErrors
import play.api.libs.json.Json
import play.api.test.Helpers._
import test.{BaseSpec, FutureTest}

class OAuthSpec extends BaseSpec with FutureTest {
  "Facebook OAuth" should {
    val controller = app.injector.instanceOf[AuthController]
    val facebookToken = "EAAbfGCbNYIQBAKe1eRtz9MgNNRjbQRI1cxZB8yEA2ehh2ZBR9po7dvTgLCo9h2TlBCgJ2w773b5NvvV5GvQgvua1YYBr3dZBfaHPmaNXbFQ3mL4OACDoYJSAZCe338YfxUGphCGTkvEpfMRmv8zJdpSyzhrgsEUZA139XF8blOg8udE15UgZBo"
    val email = "***REMOVED***"

    "Allow access for right App-key header" in {
      val oauthMethod = controller.oauth("facebook").apply(fakeRequestWithRightAppKeyHeader)
      status(oauthMethod) must not be FORBIDDEN
    }

    "Deny access for wrong App-key header" in {
      val oauthMethod = controller.oauth("facebook").apply(fakeRequestWithWrongAppKeyHeader)
      status(oauthMethod) mustBe UNAUTHORIZED
      contentAsJson(oauthMethod) mustBe JsonErrors.AppKeyIsNotFound
    }

    "Register new user" in {
      val jsRequest = Json.obj(
        "token" -> facebookToken,
        "email" -> email
      )

      val oauthMethod = controller.oauth("facebook").apply(fakeRequestWithRightAppKeyHeader.withJsonBody(jsRequest))

      status(oauthMethod) mustBe OK
      val jsResponse = contentAsJson(oauthMethod)
      (jsResponse \ "token").asOpt[String].isDefined mustBe true
      (jsResponse \ "expiresAt").asOpt[String].isDefined mustBe true
      (jsResponse \ "actionType").as[String] mustBe "registration"
    }

    "Auth if user already used oauth" in {
      val jsRequest = Json.obj(
        "token" -> facebookToken,
        "email" -> email
      )

      val oauthMethod = controller.oauth("facebook").apply(fakeRequestWithRightAppKeyHeader.withJsonBody(jsRequest))
      status(oauthMethod) mustBe OK
      val jsResponse = contentAsJson(oauthMethod)
      (jsResponse \ "token").asOpt[String].isDefined mustBe true
      (jsResponse \ "expiresAt").asOpt[String].isDefined mustBe true
      (jsResponse \ "actionType").as[String] mustBe "login"
    }
  }
}

/*
package controllers

import errorJsonBodies.JsonErrors
import play.api.libs.json.Json
import play.api.test.Helpers._
import test.{BaseSpec, FutureTest}

class OAuthSpec extends BaseSpec with FutureTest {
  "Facebook OAuth" should {
    val controller = app.injector.instanceOf[AuthController]
    val facebookToken = "EAAbfGCbNYIQBAKe1eRtz9MgNNRjbQRI1cxZB8yEA2ehh2ZBR9po7dvTgLCo9h2TlBCgJ2w773b5NvvV5GvQgvua1YYBr3dZBfaHPmaNXbFQ3mL4OACDoYJSAZCe338YfxUGphCGTkvEpfMRmv8zJdpSyzhrgsEUZA139XF8blOg8udE15UgZBo"
    val email = "***REMOVED***"

    "Allow access for right App-key header" in {
      val oauthMethod = controller.oauth("facebook").apply(fakeRequestWithRightAppKeyHeader)
      status(oauthMethod) must not be FORBIDDEN
    }

    "Deny access for wrong App-key header" in {
      val oauthMethod = controller.oauth("facebook").apply(fakeRequestWithWrongAppKeyHeader)
      status(oauthMethod) mustBe UNAUTHORIZED
      contentAsJson(oauthMethod) mustBe JsonErrors.AppKeyIsNotFound
    }

    "Register new user" in {
      val jsRequest = Json.obj(
        "token" -> facebookToken,
        "email" -> email
      )

      val oauthMethod = controller.oauth("facebook").apply(fakeRequestWithRightAppKeyHeader.withJsonBody(jsRequest))
      status(oauthMethod) mustBe OK
      val jsResponse = contentAsJson(oauthMethod)
      (jsResponse \ "token").asOpt[String].isDefined mustBe true
      (jsResponse \ "expiresAt").asOpt[String].isDefined mustBe true
      (jsResponse \ "actionType").as[String] mustBe "registration"


      whenReady(oauthMethod){ _ =>
        val jsRequest = Json.obj(
          "token" -> facebookToken,
          "email" -> email
        )

        val oauthMethod = controller.oauth("facebook").apply(fakeRequestWithRightAppKeyHeader.withJsonBody(jsRequest))
        status(oauthMethod) mustBe OK
        val jsResponse = contentAsJson(oauthMethod)
        (jsResponse \ "token").asOpt[String].isDefined mustBe true
        (jsResponse \ "expiresAt").asOpt[String].isDefined mustBe true
        (jsResponse \ "actionType").as[String] mustBe "login"
      }
    }
  }
}

 */
