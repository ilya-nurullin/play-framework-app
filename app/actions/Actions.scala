package actions

import javax.inject._

object Actions {
  val AppKeyHeaderName = "App-key"
  val AccessTokenHeaderName = "Access-token"
}

@Singleton
class Actions @Inject() (appIdFilterAction: AppIdFilterAction, accessTokenFilterAction: AccessTokenFilterAction) {
  val AuthAction = appIdFilterAction andThen accessTokenFilterAction
  val AppIdFilterAction = appIdFilterAction
}