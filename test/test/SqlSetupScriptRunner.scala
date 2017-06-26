package test

import javax.inject._

import play.api.Application
import play.api.db.slick.DatabaseConfigProvider

@Singleton
class SqlSetupScriptRunner @Inject() (app: Application, dbConfigProvider: DatabaseConfigProvider) {
  import sys.process._
  s"""cmd /C D:\\OpenServer\\modules\\database\\MySQL-5.7\\bin\\mysql.exe -uroot -Dtest_whipcake <
     |${app.getFile("test/resources/mysql_create.sql").getAbsolutePath}""".stripMargin.!!
}
