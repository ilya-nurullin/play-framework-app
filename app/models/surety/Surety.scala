package models.surety

import javax.inject._

import com.github.tototoshi.slick.MySQLJodaSupport._
import json.implicits.JsArrayMappedColumn
import models._
import org.joda.time.LocalTime
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{TableQuery, Tag}

import scala.concurrent.ExecutionContext

case class Surety(taskId: Long, guarantorId: Option[Int] = None, status: SuretyTable.SuretyStatusType = SuretyTable.FindingGuarantors,
                  allowedCount: Option[Byte], timeFrom: Option[LocalTime], timeTo: Option[LocalTime])


class SuretyTable(tag: Tag) extends Table[Surety](tag, "surety") with JsArrayMappedColumn
    with SuretyTable.SuretyStatusTypeMappedColumn {

  implicit val suretyStatusTypeColumn = suretyStatusTypeColumnType // why does not it work without this?

  def taskId = column[Long]("task_id", O.PrimaryKey)
  def guarantorId = column[Option[Int]]("guarantor_id")
  def status = column[SuretyTable.SuretyStatusType]("status")
  def allowedCount = column[Option[Byte]]("allowed_count")
  def timeFrom = column[Option[LocalTime]]("time_from")
  def timeTo = column[Option[LocalTime]]("time_to")

  def * = (taskId, guarantorId, status, allowedCount, timeFrom, timeTo).mapTo[Surety]
}

object SuretyTable {
  abstract class SuretyStatusType {
    val name: String

    override def toString = name
  }

  object SuretyStatusType {
    def apply(suretyType: String): SuretyStatusType = suretyType match {
      case FindingGuarantors.name => FindingGuarantors
      case Executing.name => Executing
      case Finished.name => Finished
    }
  }

  case object FindingGuarantors extends SuretyStatusType {
    val name = "finding_guarantors"
  }

  case object Executing extends SuretyStatusType {
    val name = "executing"
  }

  case object Finished extends SuretyStatusType {
    val name = "finished"
  }

  trait SuretyStatusTypeMappedColumn {
    implicit val suretyStatusTypeColumnType = MappedColumnType.base[SuretyStatusType, String](_.toString, SuretyStatusType(_))
  }
}

@Singleton
class SuretyDAO @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends SuretyTable.SuretyStatusTypeMappedColumn with SuretyRequestTable.SuretyRequestStatusTypeMappedColumn {

  implicit val suretyRequestStatusTypeColumn = suretyRequestStatusTypeColumnType
  implicit val suretyStatusTypeColumn = suretyStatusTypeColumnType

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  val surety = TableQuery[SuretyTable]
  val users = TableQuery[UsersTable]
  val userMetrics = TableQuery[UserMetricsTable]
  val suretyRequests = TableQuery[SuretyRequestTable]

  def getTopGuarantors() = dbConfig.db.run {
    val usersTableName = users.baseTableRow.tableName
    val suretyTableName = surety.baseTableRow.tableName
    val usersMetricsTableName = userMetrics.baseTableRow.tableName

    // todo: get top guarantors
//    sql"""SELECT users.* FROM #$usersTableName as users
//         WHERE users.can_be_guarantor = 1 AND users.id NOT IN (
//           SELECT surety.guarantor_id FROM #$suretyTableName as surety
//           WHERE surety.status = 'executing'
//           GROUP BY guarantor_id HAVING COUNT(*) >= 3
//           )""".as[User]

    users.filter(_.canBeGuarantor === true).result
  }

  def addSurety(sur: Surety) = dbConfig.db.run {
    surety += sur
  }

  def addSuretyRequest(taskId: Long, guarantors: Set[Int]) = dbConfig.db.run {
    (suretyRequests ++= guarantors.map(g => SuretyRequest(taskId, g))).transactionally
  }

  def setGuarantor(taskId: Long, guarantorId: Int) = dbConfig.db.run {
    surety.filter(_.taskId === taskId).map(_.guarantorId).update(Some(guarantorId))
  }

  def updateSuretyStatus(taskId: Long, status: SuretyTable.SuretyStatusType) = dbConfig.db.run {
    surety.filter(_.taskId === taskId).map(_.status).update(status)
  }

  def updateSuretyGuarantorWithStatus(taskId: Long, guarantorId: Int, status: SuretyTable.SuretyStatusType) = dbConfig.db.run {
    surety.filter(_.taskId === taskId).map(r => (r.guarantorId, r.status)).update((Some(guarantorId), status))
  }

  def isRequestedGuarantor(taskId: Long, guarantorId: Int) = dbConfig.db.run {
    suretyRequests.filter(r => r.taskId === taskId && r.guarantorId === guarantorId).result.headOption.collect { case r => r.isDefined }
  }

  def approveSurety(taskId: Long, guarantorId: Int) = dbConfig.db.run {
    suretyRequests.filter(r => r.taskId === taskId && r.guarantorId === guarantorId)
        .map(_.status).update(SuretyRequestTable.Approved)

    surety.filter(_.taskId === taskId).map(_.guarantorId).update(Some(guarantorId))

    suretyRequests.filter(r => r.taskId === taskId && r.guarantorId =!= guarantorId)
        .map(_.status).update(SuretyRequestTable.Rejected)
  }

  def isGuarantor(taskId: Long, guarantorId: Int) = dbConfig.db.run {
    surety.filter(r => r.taskId === taskId && r.guarantorId === guarantorId).result.headOption.collect { case r => r.isDefined }
  }

  def getSurety(taskId: Long) = dbConfig.db.run {
    surety.filter(_.taskId === taskId).result.headOption
  }
}

