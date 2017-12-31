package models.surety

import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag

case class SuretyRequest(taskId: Long, guarantorId: Int,
                         status: SuretyRequestTable.SuretyRequestStatusType = SuretyRequestTable.Considering)

object SuretyRequestTable {
  abstract class SuretyRequestStatusType {
    val name: String

    override def toString = name
  }

  object SuretyRequestStatusType {
    def apply(suretyRequestType: String) = suretyRequestType match {
      case Considering.name => Considering
      case Rejected.name => Rejected
      case Approved.name => Approved
    }
  }

  case object Considering extends SuretyRequestStatusType { val name = "considering" }
  case object Rejected extends SuretyRequestStatusType { val name = "rejected" }
  case object Approved extends SuretyRequestStatusType { val name = "approved" }

  trait SuretyRequestStatusTypeMappedColumn {
    implicit val suretyRequestStatusTypeColumnType = MappedColumnType.base[SuretyRequestStatusType, String](_.toString, SuretyRequestStatusType(_))
  }
}

class SuretyRequestTable(tag: Tag) extends Table[SuretyRequest](tag, "surety_requests")
    with SuretyRequestTable.SuretyRequestStatusTypeMappedColumn {

  implicit val suretyRequestStatusTypeColumn = suretyRequestStatusTypeColumnType

  def taskId = column[Long]("task_id")
  def guarantorId = column[Int]("guarantor_id")
  def status = column[SuretyRequestTable.SuretyRequestStatusType]("status")

  def * = (taskId, guarantorId, status).mapTo[SuretyRequest]
}