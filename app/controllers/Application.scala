package controllers

import javax.inject.Inject

import scala.jdk.OptionConverters._

import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.core.profile.UserProfile
import org.pac4j.core.client.IndirectClient

import org.pac4j.play.PlayWebContext

import org.pac4j.play.scala.SecurityComponents
import org.pac4j.play.scala.Security

import org.pac4j.oidc.profile.OidcProfile

import play.api.mvc.RequestHeader
// import play.api.mvc.ActionFilter
// import play.api.mvc.Request
// import play.api.mvc.Result

import scala.concurrent.Future
import scala.concurrent.duration._

class Application @Inject() (
    val controllerComponents: SecurityComponents
) extends Security[CommonProfile] {

  private def getProfile(implicit
      request: RequestHeader
  ): Option[OidcProfile] = {
    val webContext = new PlayWebContext(request)
    val profileManager =
      new ProfileManager(webContext, controllerComponents.sessionStore)
    val profile = profileManager.getProfile(classOf[OidcProfile])
    profile.toScala
  }

  def index = Action { implicit request =>
    Ok(views.html.index(getProfile(request)))
  }

  def googleOidcIndex =
    Secure(clients = "GoogleOidcClient") { implicit request =>
      Ok(views.html.index(getProfile(request)))
    }

  def lineOidcIndex =
    Secure(clients = "LineOidcClient") { implicit request =>
      Ok(views.html.index(getProfile(request)))
    }
}
