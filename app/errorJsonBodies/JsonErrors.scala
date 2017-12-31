package errorJsonBodies

import actions.Actions
import play.api.data.Form
import play.api.libs.json.{JsObject, JsValue, Json}

case class JsonError(error: String, data: Option[JsObject] = None)

object JsonErrors {
  implicit lazy val jsonErrorWrites = Json.writes[JsonError]
  private val appKeyHeaderName = Actions.AppKeyHeaderName.replace('-', '_').toLowerCase
  private val accessTokenHeaderName = Actions.AccessTokenHeaderName.replace('-', '_').toLowerCase

  lazy val BadCredentials = Json.toJson(JsonError("bad_credentials"))
  lazy val EmailAlreadySignedUp = Json.toJson(JsonError("email_already_signed_up"))

  lazy val NeedAppKeyHeader = Json.toJson(JsonError(s"need_${appKeyHeaderName}_header"))
  lazy val AppKeyIsNotFound = Json.toJson(JsonError(s"${appKeyHeaderName}_is_not_found"))
  lazy val AppKeyIsBanned = Json.toJson(JsonError(s"${appKeyHeaderName}_is_banned"))

  lazy val AccessTokenIsNotValid = Json.toJson(JsonError(s"${accessTokenHeaderName}_is_not_valid"))
  lazy val NeedAccessTokenHeader = Json.toJson(JsonError(s"need_${accessTokenHeaderName}_header"))

  lazy val ChangingSomeoneElsesObject = Json.toJson(JsonError("changing_someone_elses_object"))
  lazy val GettingSomeoneElsesObject = Json.toJson(JsonError("getting_someone_elses_object"))
  lazy val DeletingSomeoneElsesObject = Json.toJson(JsonError("deleting_someone_elses_object"))

  lazy val EmptyRequest = Json.toJson(JsonError("empty_request"))
  lazy val LoginDuplication = Json.toJson(JsonError("login_duplication"))
  lazy val EmailDuplication = Json.toJson(JsonError("email_duplication"))

  lazy val OAuthEmptyEmail = Json.toJson(JsonError("oauth_empty_email")) // for registration
  lazy val OAuthFailed = Json.toJson(JsonError("oauth_failed"))
  lazy val OAuthAlreadySignedUp = Json.toJson(JsonError("oauth_already_signed_up"))
  lazy val OAuthEmailConflict = Json.toJson(JsonError("oauth_email_conflict"))

  lazy val WrongPassword = Json.toJson(JsonError("wrong_password"))

  lazy val AlreadySynchronized = Json.toJson(JsonError("already_synchronized"))

  lazy val GuarantorToMyself = Json.toJson(JsonError("guarantor_to_myself"))
  lazy val TaskIsNotOpenForSurety = Json.toJson(JsonError("task_is_not_open_for_surety"))

  def BadData(errors: JsObject) = Json.toJson(JsonError("bad_data", Some(errors)))
  def BadData(errors: JsValue) = Json.toJson(JsonError("bad_data", Some(errors.asInstanceOf[JsObject])))
  def BadDataNonOwner(fieldName: String) = BadData(Json.obj(fieldName -> "nonOwner"))

  def FormWithErrorsToJson[T](formWithErrors: Form[T]) = {
    val errors = formWithErrors.errors.foldLeft(Map[String, String]()) { (m, e) => m + (e.key -> e.message) }
    JsonErrors.BadData(Json.toJson(errors))
  }

}
