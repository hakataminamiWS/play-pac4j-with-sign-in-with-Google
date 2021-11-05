package controllers

import javax.inject.Inject
import org.pac4j.core.context.{HttpConstants, WebContext}
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.exception.http.HttpAction
import org.pac4j.play.http.PlayHttpActionAdapter
import org.pac4j.play.PlayWebContext
import play.api.Logger
import play.api.mvc.Results

case class ErrorHandler(sessionStore: SessionStore) extends PlayHttpActionAdapter {
  val log: Logger = Logger(this.getClass())
  implicit val sStore = sessionStore
  override def adapt(action: HttpAction, context: WebContext): play.mvc.Result = {
    val playWebContext = context.asInstanceOf[PlayWebContext]
    implicit val request = playWebContext.getNativeScalaRequest()

    if (action != null && action.getCode == HttpConstants.UNAUTHORIZED) {
      playWebContext.supplementResponse(
        Results.Ok(views.html.demoPage(getProfile(request))).asJava
      )
    } else if (action != null && action.getCode == HttpConstants.FORBIDDEN) {
      playWebContext.supplementResponse(
        Results.Ok(views.html.demoPage(getProfile(request))).asJava
      )
    } else {
      super.adapt(action, context)
    }
  }
}
