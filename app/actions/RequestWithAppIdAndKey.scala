package actions

import play.api.mvc.{Request, WrappedRequest}

class RequestWithAppIdAndKey[A](val appId: Int,val appKey: String, val baseRequest: Request[A])  extends WrappedRequest(baseRequest)
