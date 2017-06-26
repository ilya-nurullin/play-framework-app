package test

import org.scalatest.concurrent.{Futures, ScalaFutures}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest

abstract class BaseSpec extends PlaySpec with GuiceOneAppPerSuite {
  val rightAccessToken = "PjdSyBX62WSq8b1IEOEFMfsjBYZcpP"
  val rightAppKey = "1"

  val fakeRequestWithRightAppKeyHeader = FakeRequest().withHeaders("App-key" -> rightAppKey)
  val fakeRequestWithWrongAppKeyHeader = FakeRequest().withHeaders("App-key" -> "wrong helpers key")

  val fakeRequestWithRightAuthHeaders = FakeRequest().withHeaders(
    "App-key" -> rightAppKey, "Access-token" -> rightAccessToken)

  val fakeRequestWith_RightAppKey_WrongAccessToken = FakeRequest().withHeaders(
    "App-key" -> rightAppKey, "Access-token" -> "wrong access token")

  val fakeRequestWith_WrongAppKey_RightAccessToken = FakeRequest().withHeaders(
    "App-key" -> "wrong helpers key", "Access-token" -> rightAccessToken)

  val fakeRequestWithWrongAppKeyAccessToken = FakeRequest().withHeaders(
    "App-key" -> "wrong helpers key", "Access-token" -> "wrong access token")

//  override def fakeApplication() = new GuiceApplicationBuilder().configure(Map(
//    "slick.dbs.default.driver" -> "slick.driver.H2Driver$",
//    "slick.dbs.default.db.driver" -> "org.h2.Driver",
//    "slick.dbs.default.db.url" -> "jdbc:h2:mem:play;MODE=MYSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;INIT=RUNSCRIPT FROM 'test/resources/create.sql'\\;",
//    "slick.dbs.default.db.numThreads" -> "2",
//    "slick.dbs.default.db.queueSize" -> "2"
//  )).in(Mode.Test).build()

  app.injector.instanceOf[SqlSetupScriptRunner]

  override def fakeApplication() = new GuiceApplicationBuilder().configure(Map(
    "slick.dbs.default.driver" -> "slick.driver.MySQLDriver$",
    "slick.dbs.default.db.driver" -> "com.mysql.cj.jdbc.Driver",
    "slick.dbs.default.db.url" -> "jdbc:mysql://root:***REMOVED***@localhost/test_whipcake?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
    "slick.dbs.default.db.numThreads" -> "6",
    "slick.dbs.default.db.queueSize" -> "6"/*,
    "db.default.driver" -> "com.mysql.cj.jdbc.Driver",
    "db.default.url" -> "jdbc:mysql://root:***REMOVED***@localhost/test-whipcake?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
  */)).in(Mode.Test).build()
}

trait FutureTest extends Futures with ScalaFutures