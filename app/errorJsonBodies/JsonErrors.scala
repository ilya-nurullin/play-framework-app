package errorJsonBodies

import actions.Actions
import play.api.libs.json.{JsObject, Json}

case class JsonError(error: String, data: Option[JsObject] = None) {
}

object JsonErrors {
  implicit lazy val jsonErrorWrites = Json.writes[JsonError]
  private val appKeyHeaderName = Actions.AppKeyHeaderName.replace('-', '_').toLowerCase
  private val accessTokenHeaderName = Actions.AccessTokenHeaderName.replace('-', '_').toLowerCase

  lazy val JsonExpected = Json.toJson(JsonError("json_expected"))
  lazy val EmailOrPasswordNotFound = Json.toJson(JsonError("email_or_password_not_found"))
  lazy val CannotCreateUser = Json.toJson(JsonError("cannot_create_user"))
  lazy val BadCredentials = Json.toJson(JsonError("bad_credentials"))
  lazy val EmailAlreadySignedUp = Json.toJson(JsonError("email_already_signed_up"))

  lazy val NeedAppKeyHeader = Json.toJson(JsonError(s"need_${appKeyHeaderName}_header"))
  lazy val AppKeyIsNotFound = Json.toJson(JsonError(s"${appKeyHeaderName}_is_not_found"))
  lazy val AppKeyIsBanned = Json.toJson(JsonError(s"${appKeyHeaderName}_is_banned"))

  lazy val AccessTokenIsNotValid = Json.toJson(JsonError(s"${accessTokenHeaderName}_is_not_valid"))
  lazy val NeedAccessTokenHeader = Json.toJson(JsonError(s"need_${accessTokenHeaderName}_header"))


}
