package actions

import play.api.mvc.{Request, WrappedRequest}

class UserRequest[A](val userId: Int, val appId: Int, val appKey: String, val baseRequest: Request[A]) extends WrappedRequest(baseRequest)
