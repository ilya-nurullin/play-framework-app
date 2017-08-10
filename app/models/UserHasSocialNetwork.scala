package models

import javax.inject._

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{TableQuery, Tag}

import scala.concurrent.ExecutionContext

case class UserHasSocialNetwork(userId: Int, networkName: String, userNetworkId: String, email: Option[String])

class UserHasSocialNetworkTable(tag: Tag) extends Table[UserHasSocialNetwork](tag, "user_has_social_network"){
  def userId = column[Int]("user_id")
  def networkName = column[String]("network_name")
  def userNetworkId = column[String]("user_network_id")
  def email = column[Option[String]]("email")

  def * = (userId, networkName, userNetworkId, email) <> (UserHasSocialNetwork.tupled, UserHasSocialNetwork.unapply)
}

@Singleton
class UserHasSocialNetworkDAO @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  val userHasSocialNetwork = TableQuery[UserHasSocialNetworkTable]

  def isSocialNetworkAllowedByUser(userId: Int, networkName: String) = dbConfig.db.run {
    userHasSocialNetwork.filter(r => r.userId === userId && r.networkName === networkName).result.headOption.collect {
      case opt => opt.isDefined
    }
  }

  def addSocialNetworkToUser(network: UserHasSocialNetwork) = dbConfig.db.run {
    userHasSocialNetwork += network
  }

  def getUserIdByNetworkNameAndNetworkUserId(networkName: String, userNetworkId: String) = dbConfig.db.run {
    userHasSocialNetwork
        .filter(r => r.networkName === networkName && r.userNetworkId === userNetworkId)
        .map(_.userId)
        .result.headOption
  }

  def findUserByEmail(email: String) = dbConfig.db.run {
    userHasSocialNetwork
        .filter(_.email === email)
        .map(_.userId)
        .result
        .collect {
          case seq: Seq[Int] =>
            if (seq.isEmpty)
              None
            else {
              val first = seq.head
              if (seq.forall(_ == first))
                Some(first)
              else
                None
            }
        }
  }

  def isUserNetworkIdAlreadySignedUp(userNetworkId: String) = dbConfig.db.run {
    userHasSocialNetwork
        .filter(_.userNetworkId === userNetworkId)
        .result.headOption
        .collect {
          case opt => opt.isDefined
        }
  }

  def updateUsersNetworkEmail(userId: Int, networkName: String,  email: String) = dbConfig.db.run {
    userHasSocialNetwork
        .filter(r => r.userId === userId && r.networkName === networkName)
        .map(_.email)
        .update(Some(email))
  }
}