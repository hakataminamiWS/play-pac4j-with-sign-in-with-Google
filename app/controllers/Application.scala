package controllers

import javax.inject.Inject
import scala.jdk.OptionConverters._

import org.pac4j.play.scala.SecurityComponents
import org.pac4j.play.scala.Security
import org.pac4j.core.profile.CommonProfile
import play.api.mvc.RequestHeader
import org.pac4j.play.PlayWebContext
import org.pac4j.core.profile.ProfileManager
import org.pac4j.core.profile.UserProfile
import org.pac4j.core.client.IndirectClient
import play.cache.AsyncCacheApi
import org.pac4j.core.context.session.SessionStore
import org.pac4j.play.store.PlayCacheSessionStore

class Application @Inject() (
    val controllerComponents: SecurityComponents,
) extends Security[CommonProfile] {

  private def getProfile(implicit
      request: RequestHeader
  ): Option[UserProfile] = {
    val webContext = new PlayWebContext(request)
    val profileManager =
      new ProfileManager(webContext, controllerComponents.sessionStore)
    val profile = profileManager.getProfile()
    profile.toScala
  }

  def index = Action { implicit request =>
    Ok(views.html.index(getProfile(request)))
  }

  def googleOidcIndex = Secure("GoogleOidcClient") { implicit request =>
    Ok(views.html.index(getProfile(request)))
  }

}
