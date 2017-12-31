package = input("package: models[.]: ").strip()
modelName = input("Model name: ")
tableName = input("Table name: ")
file = ""

if (package == ""):
    package = "models"
else:
    package = "models." + package

package = "package " + package

props = []

while (True):
    prop = input("enter property(name type[ dbName]): ").strip()
    
    if (prop == ""):
        break

    props.append(prop.split())

file += package + "\n\n"

file += """import javax.inject._

import com.github.tototoshi.slick.MySQLJodaSupport._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{TableQuery, Tag}

import scala.concurrent.ExecutionContext

"""

modelCaseClass = "case class " + modelName + "(" + ', '.join(prop[0]+": "+prop[1]  for prop in props) + ")"

file += modelCaseClass + "\n\n"

defs = '\n  '.join("def " + prop[0] + " = column[" + prop[1] + "](\"" + (prop[0] if (len(prop) != 3) else prop[2]) + "\")"  for prop in props) 
paramsList = ', '.join(prop[0] for prop in props)

modelTable = """class {model_name}Table(tag: Tag) extends Table[{model_name}](tag, "{table_name}"){{
  {defs}

  def * = ({params_list}).mapTo[{model_name}]
}}

""".format(model_name = modelName, table_name = tableName, defs = defs, params_list = paramsList)

file += modelTable

tableDAO = """@Singleton
class {model_name}DAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {{
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  val table = TableQuery[{model_name}Table]
}}
""".format(model_name = modelName)

file += tableDAO


print(file)

"""
package models

import javax.inject._

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.Tag
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext

case class ApiApp(id: Int, key: String, isBanned: Boolean)

class ApiAppTable(tag: Tag) extends Table[ApiApp](tag, "api_apps") {
  def id = column[Int]("id", O.PrimaryKey)
  def key = column[String]("key", O.Unique)
  def isBanned = column[Boolean]("is_banned")

  def * = (id, key, isBanned) <> (ApiApp.tupled, ApiApp.unapply)
}

@Singleton
class ApiAppDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val apiApp: TableQuery[ApiAppTable] = TableQuery[ApiAppTable]
  import dbConfig.profile.api._

  def getApp(key: String) = dbConfig.db.run {
    apiApp.filter(_.key === key).take(1).result.headOption
  }

  def isAppOk(key: String) = getApp(key).map { optApp =>
    optApp.exists { app => !app.isBanned }
  }
}"""