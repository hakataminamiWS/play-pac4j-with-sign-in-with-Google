package controllers

import javax.inject.Inject
import org.pac4j.core.authorization.authorizer.RequireAnyPermissionAuthorizer
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.exception.http.WithLocationAction
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.core.profile.UserProfile
import org.pac4j.core.util.Pac4jConstants
import org.pac4j.oidc.profile.OidcProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala.Security
import org.pac4j.play.scala.SecurityComponents
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.jdk.OptionConverters._

class Application @Inject() (
    val controllerComponents: SecurityComponents
) extends Security[CommonProfile] {

  private def getProfile(implicit
      request: RequestHeader
  ): Option[OidcProfile] = {
    val context = new PlayWebContext(request)
    val profileManager =
      new ProfileManager(context, sessionStore)
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

  def SignInWithGoogleAddAuthority =
    Secure(
      clients = "GoogleOidcClientWithAuthority",
      authorizers = "test"
    ) { implicit request =>
      Ok(views.html.showAuthority(getProfile(request)))
    }

  def demoPage = Action { implicit request =>
    Ok(views.html.demoPage(getProfile(request)))
  }

  def showAuthority = Action { implicit request =>
    Ok(views.html.showAuthority(getProfile(request)))
  }

  def checkAuthority = {
    val testAuthorizer = new RequireAnyPermissionAuthorizer("test")
    config.addAuthorizer("testAuthorizer", testAuthorizer)
    Secure(clients = null, authorizers = "testAuthorizer") { implicit request =>
      Ok(views.html.checkAuthority(getProfile(request)))
    }
  }

  def addAuthorityForSignInUser =
    Secure { implicit request =>
      val context = new PlayWebContext(request)
      val profileManager =
        new ProfileManager(context, sessionStore)

      val optProfile = getProfile(request)
        .map { profile =>
          profile.addPermission("test")
          profile
        }
        .map { profile =>
          profileManager.save(
            true, // saveInSession
            profile,
            true // multiProfile
          )
          profile
        }

      context.supplementResponse(
        Ok(views.html.addAuthorityForSignInUser(optProfile))
      )
    }

}
