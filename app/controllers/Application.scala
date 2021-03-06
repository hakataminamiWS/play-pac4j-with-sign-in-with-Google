package controllers

import authorization._
import authorization.authorizer.RequireAnyNewerRole
import authorization.repository.AuthorityRepository
import authorization.roles.Owner
import javax.inject.Inject
import javax.inject.Named
import oidc.profile.OidcProfile
import org.pac4j.core.authorization.authorizer.Authorizer
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.config.Config
import org.pac4j.core.exception.http.WithLocationAction
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.core.util.Pac4jConstants
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala.Security
import org.pac4j.play.scala.SecurityComponents
import play.api.cache.AsyncCacheApi
import play.api.Logger
import play.api.Logging
import play.api.mvc.AnyContent
import play.api.mvc.RequestHeader
import scala.jdk.OptionConverters._

class Application @Inject() (
    val controllerComponents: SecurityComponents,
    @Named("repoCache") repo: AuthorityRepository,
    cache: AsyncCacheApi,
    dataSet: DataSetForDemo
) extends Security[OidcProfile]
    with Logging {

  implicit val ec = controllerComponents.executionContext
  implicit val sStore = controllerComponents.sessionStore

  def index = Action { implicit request: RequestHeader =>
    Ok(views.html.index(getProfile(request)))
  }

  private def putIfAbsentAuthorizer(
      config: Config,
      name: String,
      authorizer: => Authorizer
  ): Unit = {
    if (!config.getAuthorizers().containsKey(name)) {
      config.addAuthorizer(name, authorizer)
    }
  }

  def googleOidcIndex =
    Secure(clients = "GoogleOidcClient") { implicit request: AuthenticatedRequest[AnyContent] =>
      val profile = profiles.headOption
      Ok(views.html.index(profile))
    }
  def googleOidcIndexWithAuthorizer = {
    val authorizerName = dataSet.ownerAuthorizerName
    putIfAbsentAuthorizer(
      config,
      authorizerName,
      RequireAnyNewerRole.Of(Owner)(
        dataSet.resourceIdName,
        repo.getTypedIdRoleAndUpdateAtMap
      )
    )

    Secure(clients = "GoogleOidcClient", authorizers = authorizerName) {
      implicit request: AuthenticatedRequest[AnyContent] =>
        val profile = profiles.headOption
        Ok(views.html.index(profile))
    }
  }

  def lineOidcIndex =
    Secure(clients = "LineOidcClient") { implicit request: AuthenticatedRequest[AnyContent] =>
      val profile = profiles.headOption
      Ok(views.html.index(profile))
    }
  def lineOidcIndexWithAuthorizer = {
    val authorizerName = dataSet.anyAuthorizerName
    putIfAbsentAuthorizer(
      config,
      authorizerName,
      RequireAnyNewerRole(
        dataSet.resourceIdName,
        repo.getTypedIdRoleAndUpdateAtMap
      )
    )

    Secure(clients = "LineOidcClient", authorizers = authorizerName) {
      implicit request: AuthenticatedRequest[AnyContent] =>
        val profile = profiles.headOption
        Ok(views.html.index(profile))
    }
  }

  def enforceSignInWithGoogle = {
    enforceSignIn(_: Option[String])(oidcClient = "GoogleOidcClient")
  }

  def enforceSignInWithLine = {
    enforceSignIn(_: Option[String])(oidcClient = "LineOidcClient")
  }

  def enforceSignIn(optQueryRequestURL: Option[String])(oidcClient: String) =
    Action { implicit request: RequestHeader =>
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

  private def showAllCache: Unit = {
    val loggerName = this.getClass().toString + "#showCache"
    val logger = Logger(loggerName)
    val cacheKey = dataSet.resourceIdName
    cache.get[TypedIdRoleAndUpdateAtMap](cacheKey).map {
      case Some(typedMap) =>
        logger.debug(s"cached TypedIdRoleAndUpdateAtMap: ${typedMap}")
      case None => logger.debug("No cache")
    }
  }

  def demoPage = Action { implicit request: RequestHeader =>
    showAllCache

    // authority check logic add
    val authorized: Boolean =
      getProfile(request)
        .map(profile =>
          RequireAnyNewerRole
            .isProfileSignInAfterUpdate(
              profile,
              repo
                .getTypedIdRoleAndUpdateAtMap(dataSet.resourceIdName)
            )
        )
        .getOrElse(false)

    // boolean: html content read or not
    Ok(views.html.demoPage(getProfile(request), authorized))
  }

  def addCache = Action { implicit request: RequestHeader =>
    val cacheKey = dataSet.resourceIdName
    cache
      .set(
        cacheKey,
        dataSet.allowedMap
      )
      .foreach(_ => showAllCache)
    Ok(views.html.demoPage(getProfile(request)))
  }

  def removeCache = Action { implicit request: RequestHeader =>
    cache
      .removeAll()
      .foreach(_ => showAllCache)
    Ok(views.html.demoPage(getProfile(request)))
  }
}

import play.api.Configuration
class DataSetForDemo @Inject() (configuration: Configuration) extends Logging {
  import authorization.roles.Owner
  import authorization.roles.RoleAndUpdateAt
  import authorization.roles.Contributor

  val ownerAuthorizerName = "ownerAuthorizer"
  val anyAuthorizerName = "anyAuthorizer"
  val resourceIdName = "resourceId"

  val olderParseableString = "2000-01-01T01:02:03.456789Z"
  val newerParseableString = "2111-01-01T01:02:03.456789Z"

  val javaTimeInstant = java.time.Instant.parse(olderParseableString)
  // val javaTimeInstant = java.time.Instant.parse(newerParseableString)

  val typedIdGoogle =
    configuration.getOptional[String](
      "dataset.secret.demo.typedId.google"
    ) match {
      case Some(str) => str
      case None => {
        val value = "typedIdGoogle"
        logger.warn(s"${value} not found in application.conf")
        value
      }
    }
  val typedIdLine =
    configuration.getOptional[String](
      "dataset.secret.demo.typedId.line"
    ) match {
      case Some(str) => str
      case None => {
        val value = "typedIdLine"
        logger.warn(s"${value} not found in application.conf")
        value
      }
    }

  val allowedMap: TypedIdRoleAndUpdateAtMap =
    Map(
      typedIdGoogle -> RoleAndUpdateAt(Owner, javaTimeInstant),
      typedIdLine -> RoleAndUpdateAt(Contributor, javaTimeInstant)
    )
}
