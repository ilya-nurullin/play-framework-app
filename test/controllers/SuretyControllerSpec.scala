package controllers

import errorJsonBodies.JsonErrors
import models.surety.{SuretyDAO, SuretyTable}
import play.api.libs.json.{JsArray, Json}
import test.BaseSpec
import play.api.test.Helpers._
import test._

class SuretyControllerSpec extends BaseSpec with AuthActionBehaviors with FutureTest {

  "SuretyController" should {
    val controller = app.injector.instanceOf[SuretyController]

    "getTopGuarantors" in {
      val getTopGuarantors = controller.topGuarantors().apply(fakeRequestWithRightAuthHeaders)
      status(getTopGuarantors) mustBe OK
      contentAsJson(getTopGuarantors) mustBe a[JsArray]
    }

    "sendSuretyRequest" should {
      behave like authAction(controller.sendSuretyRequest(1))
      behave like filterOnlyObjectOwnerAllowed(controller.sendSuretyRequest(1), controller.sendSuretyRequest(2))

      "can't be guarantor to myself" in {
        val jsonRequest = Json.obj("guarantors" -> Json.arr(1, 2, 3))
        val sendSuretyRequest = controller.sendSuretyRequest(1).apply(fakeRequestWithRightAuthHeaders
            .withJsonBody(Json.toJson(jsonRequest)))

        status(sendSuretyRequest) mustBe BAD_REQUEST
        contentAsJson(sendSuretyRequest) mustBe JsonErrors.GuarantorToMyself
      }

      "without count and times" in {
        val jsonRequest = Json.obj("guarantors" -> Json.arr(2))
        val sendSuretyRequest = controller.sendSuretyRequest(1).apply(fakeRequestWithRightAuthHeaders
            .withJsonBody(Json.toJson(jsonRequest)))

        status(sendSuretyRequest) mustBe NO_CONTENT
      }

      "with count and times" in {
        val jsonRequest = Json.obj("guarantors" -> Json.arr(3), "timeFrom" -> "12:00:00", "timeTo" -> "15:00:00",
          "allowedCount" -> 10)
        val sendSuretyRequest = controller.sendSuretyRequest(3).apply(fakeRequestWithRightAuthHeaders
            .withJsonBody(Json.toJson(jsonRequest)))

        status(sendSuretyRequest) mustBe NO_CONTENT

        whenReady(app.injector.instanceOf[models.surety.SuretyDAO].getSurety(3)) { suretyOpt =>
          suretyOpt.isDefined mustBe true
          val surety = suretyOpt.get
          surety.allowedCount.isDefined mustBe true
          surety.allowedCount.get mustBe 10

          surety.timeFrom.isDefined mustBe true
          surety.timeFrom.get mustBe new org.joda.time.LocalTime("12:00:00")

          surety.timeTo.isDefined mustBe true
          surety.timeTo.get mustBe new org.joda.time.LocalTime("15:00:00")

        }
      }

      "task is closed  for surety" in {
        val jsonRequest = Json.obj("guarantors" -> Json.arr(2, 3))
        val sendSuretyRequest = controller.sendSuretyRequest(4).apply(fakeRequestWithRightAuthHeaders
            .withJsonBody(Json.toJson(jsonRequest)))

        status(sendSuretyRequest) mustBe BAD_REQUEST
        contentAsJson(sendSuretyRequest) mustBe JsonErrors.TaskIsNotOpenForSurety
      }
    }

    "approveSuretyRequest" in {
      val approveSuretyRequest = controller.approveSuretyRequest(1).apply(fakeRequestWithRightAuthHeaders_user2)

      status(approveSuretyRequest) mustBe NO_CONTENT
      whenReady(app.injector.instanceOf[SuretyDAO].getSurety(1)) { suretyOpt =>
        suretyOpt.isDefined mustBe true
        val surety = suretyOpt.get
        surety.guarantorId mustBe Some(2)
        surety.status mustBe SuretyTable.Executing
      }
    }


  }

}
