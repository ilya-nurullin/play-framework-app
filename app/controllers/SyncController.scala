package controllers

import javax.inject._

import actions.Actions
import models.LastSyncIdDAO
import play.api.libs.json.Json
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

@Singleton
class SyncController @Inject() (actions: Actions, lastSyncIdDAO: LastSyncIdDAO)(implicit ec: ExecutionContext) extends InjectedController {
  def lastSyncId() = actions.AuthAction.async { request =>
    for {
      lastSyncIdOpt <- lastSyncIdDAO.getLastSyncId(request.userId, request.appId)
    } yield Ok(Json.toJson(lastSyncIdOpt))
  }
}
