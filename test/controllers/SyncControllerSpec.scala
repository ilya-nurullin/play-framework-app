package controllers

import play.api.libs.json.JsNull
import play.api.test.Helpers._
import test.{BaseSpec, FutureTest}

class SyncControllerSpec extends BaseSpec with FutureTest with AuthActionBehaviors {
  "SyncController" should {
    val controller = app.injector.instanceOf[SyncController]

    behave like authAction(controller.lastSyncId())

    "GET" should {
      "Response null if syncId does not exist" in {
        val syncMethod = controller.lastSyncId().apply(fakeRequestWithRightAuthHeaders)
        status(syncMethod) mustBe OK
        contentAsJson(syncMethod) mustBe JsNull
      }

      "Response with long number" in {
        whenReady(app.injector.instanceOf[models.LastSyncIdDAO].createNewLastSyncId(1, 1, 3)) { _ =>
          val syncMethod = controller.lastSyncId().apply(fakeRequestWithRightAuthHeaders)
          status(syncMethod) mustBe OK
          contentAsJson(syncMethod).as[Long] mustBe 3
        }
      }
    }
  }
}
