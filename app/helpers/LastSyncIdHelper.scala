package helpers

import javax.inject._

import actions.UserRequest
import errorJsonBodies.JsonErrors
import models.LastSyncIdDAO
import play.api.mvc.{Result, Results}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LastSyncIdHelper @Inject() (lastSyncIdDAO: LastSyncIdDAO)(implicit ec: ExecutionContext) extends Results {

  def checkSyncId(lastSyncId: Option[Long])(block: => Future[Result])(implicit request: UserRequest[_]): Future[Result] = {
    lastSyncId.map(getSyncId =>
      lastSyncIdDAO.getLastSyncId(request.userId, request.appId).flatMap { syncIdOpt =>
        if (syncIdOpt.isEmpty) {
          lastSyncIdDAO.createNewLastSyncId(request.userId, request.appId, getSyncId)
          block
        }
        else {
          val syncId = syncIdOpt.get
          if (syncId >= getSyncId)
            Future.successful(Conflict(JsonErrors.AlreadySynchronized))
          else {
            val res = block
            lastSyncIdDAO.updateLastSyncId(request.userId, request.appId, getSyncId)
            res
          }
        }
      }
    ) getOrElse Future.successful(Conflict(JsonErrors.AlreadySynchronized))
  }

}
