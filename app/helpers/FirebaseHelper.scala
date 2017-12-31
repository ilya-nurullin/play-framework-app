package helpers

import javax.inject._

import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.language.implicitConversions

@Singleton
class FirebaseHelper @Inject()(_ws: WSClient, messagesApi: MessagesApi) {
  val url = "https://fcm.googleapis.com/fcm/send"
  val key = "AAAAUUIN5wE:APA91bHcsry6RzNchqjW_5qSwVLRo3AdbHP82fovERmzBRJEtS3vf_u2CkfYWH8EzduPjEeHlXflj4t-FCByhdpDOo-jdPuND11tfr0p9bvXdZ4ZEBkPDmy1P9hbCksNKFKk-KynkrZz"

  val ws = _ws.url(url).withHttpHeaders("Authorization" -> s"key=$key", "Content-Type" -> "application/json")

  def sendSuretyRequest(to: String, lang: String) = {
    val jsRequest = Json.obj(
      "to" -> to,
      "title" -> messagesApi("firebase.suretyRequest.title")(lang),
      "body" -> messagesApi("firebase.suretyRequest.body")(lang)
    )
    ws.post(jsRequest)
  }

  def sendApproveRequest(to: String, lang: String) = {
    val jsRequest = Json.obj(
      "to" -> to,
      "title" -> messagesApi("firebase.approveRequest.title")(lang),
      "body" -> messagesApi("firebase.approveRequest.body")(lang)
    )
    ws.post(jsRequest)
  }

  def sendWhip(to: String, message: Option[String], lang: String) = {
    val body = message.map(messagesApi("firebase.sendWhip.bodyWithMessage", _)(lang)) getOrElse messagesApi("firebase.sendWhip.bodyWoMessage")(lang)
    val jsRequest = Json.obj(
      "to" -> to,
      "title" -> messagesApi("firebase.sendWhip.title")(lang),
      "body" -> body
    )
    ws.post(jsRequest)
  }

  implicit def string2Lang(lang: String): Lang = Lang(lang)

}
