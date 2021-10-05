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
import play.api.mvc.ActionFilter
import play.api.mvc.Request

import play.api.mvc.Result
import scala.concurrent.Future

import play.api.cache.redis.CacheApi
import scala.concurrent.duration._

class Application @Inject() (
    val controllerComponents: SecurityComponents,
    cache: CacheApi
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

  def googleOidcIndex =
    (Secure("GoogleOidcClient") andThen refreshExpirationOneHour) {
      implicit request =>
        Ok(views.html.index(getProfile(request)))
    }

  def lineOidcIndex =
    (Secure("LineOidcClient") andThen refreshExpirationOneHour) {
      implicit request =>
        Ok(views.html.index(getProfile(request)))
    }

  private object refreshExpirationOneHour extends ActionFilter[Request] {
    implicit def executionContext =
      controllerComponents.executionContext
    override protected def filter[A](
        request: Request[A]
    ): Future[Option[Result]] = {
      val webContext = new PlayWebContext(request)
      // refreshes expiration of the key if present
      sessionStore
        .getSessionId(webContext, false)
        .toScala
        .map(sessionId => cache.expire(sessionId, 1.hour))
      Future(None)
    }
  }
}
