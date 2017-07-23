package models

import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag

case class City(id: Int, name: String, countryId: Int)

class CitiesTable(tag: Tag) extends Table[City](tag, "cities") {
  def id = column[Int]("id")
  def name = column[String]("name")
  def countryId = column[Int]("country_id")

  def * = (id, name, countryId) <> ( (City.apply _).tupled, City.unapply )
}

