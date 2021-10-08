package controllers

import javax.inject.Inject

import scala.jdk.OptionConverters._

import org.pac4j.core.client.IndirectClient

import org.pac4j.core.credentials.Credentials

import org.pac4j.core.exception.http.WithLocationAction

import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.core.profile.UserProfile

import org.pac4j.core.util.Pac4jConstants

import org.pac4j.play.PlayWebContext

import org.pac4j.play.scala.SecurityComponents
import org.pac4j.play.scala.Security

import org.pac4j.oidc.profile.OidcProfile

import play.api.mvc.RequestHeader

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

  def enforceSignInWithGoogle = {
    enforceSignIn(_: Option[String])(oidcClient = "GoogleOidcClient")
  }

  def enforceSignInWithLine = {
    enforceSignIn(_: Option[String])(oidcClient = "LineOidcClient")
  }

  def enforceSignIn(optQueryRequestURL: Option[String])(oidcClient: String) =
    Action { implicit request =>
      val context = new PlayWebContext(request)

      val scheme = if (request.connection.secure) "https" else "http"
      // set request URL for callback
      optQueryRequestURL.map(requestURL =>
        controllerComponents.sessionStore.set(
          context,
          Pac4jConstants.REQUESTED_URL,
          scheme + "://" + request.host + requestURL
        )
      )

      // enforce sign in
      val client = config.getClients
        .findClient(oidcClient)
        .get()
        .asInstanceOf[IndirectClient]
      val location = client
        .getRedirectionAction(context, sessionStore)
        .get
        .asInstanceOf[WithLocationAction]
        .getLocation

      context.supplementResponse(Redirect(location))
    }

  def demoPage = Action { implicit request =>
    val context = new PlayWebContext(request)
    controllerComponents.sessionStore.set(
      context,
      Pac4jConstants.REQUESTED_URL,
      context.getFullRequestURL()
    )

    Ok(views.html.demoPage(getProfile(request)))
  }
}
