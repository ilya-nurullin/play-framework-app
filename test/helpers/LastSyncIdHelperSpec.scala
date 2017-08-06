package helpers

import play.api.mvc.Results
import play.api.test.FakeRequest
import test.{BaseSpec, FutureTest}
import actions.UserRequest
import errorJsonBodies.JsonErrors
import org.scalatest.time.SpanSugar._

import scala.concurrent.Future

class LastSyncIdHelperSpec extends BaseSpec with FutureTest with Results {
  "LastSyncIdHelper" should {
    val helper = app.injector.instanceOf[LastSyncIdHelper]
    val fakeRequest = new UserRequest(1, 1, "asda", FakeRequest())
    val result = Ok("ok")

    "Create new syncId if not exists" in {
      whenReady(
        {helper.checkSyncId(Some(1)) {
          Future.successful(result)
        }(fakeRequest)}
        , timeout(1 second)
      )( _ mustBe result )
    }

    "Work fine" in {
      whenReady(
        {helper.checkSyncId(Some(2)) {
          Future.successful(result)
        }(fakeRequest)}
        , timeout(1 second)
      )( _ mustBe result )
    }

    "Fail on duplication" in {
      whenReady(
        {helper.checkSyncId(Some(1)) {
          Future.successful(result)
        }(fakeRequest)}
        , timeout(1 second)
      )( _ mustBe Conflict(JsonErrors.AlreadySynchronized) )

      whenReady(
        {helper.checkSyncId(Some(2)) {
          Future.successful(result)
        }(fakeRequest)}
        , timeout(1 second)
      )( _ mustBe Conflict(JsonErrors.AlreadySynchronized) )
    }
  }
}
