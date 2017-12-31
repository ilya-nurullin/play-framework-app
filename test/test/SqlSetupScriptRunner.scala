package test

import javax.inject._

import play.api.{Application, Environment}
import play.api.db.slick.DatabaseConfigProvider

@Singleton
class SqlSetupScriptRunner @Inject() (app: Application, env: Environment, dbConfigProvider: DatabaseConfigProvider) {
  import sys.process._
  s"""cmd /C D:\\OSPanel\\modules\\database\\MySQL-5.7-x64\\bin\\mysql.exe -uroot -Dtest_whipcake <
     |${env.getFile("test/resources/mysql_create.sql").getAbsolutePath}""".stripMargin.!!
}
