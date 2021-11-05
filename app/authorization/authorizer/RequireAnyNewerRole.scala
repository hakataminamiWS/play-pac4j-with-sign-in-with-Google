package authorization.authorizer

import authorization._
import authorization.repository.AwaitError
import authorization.repository.Error
import authorization.roles.Role
import authorization.roles.RoleAndUpdateAt
import oidc.profile.OidcProfile
import org.pac4j.core.authorization.authorizer.ProfileAuthorizer
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.UserProfile
import play.api.Logging
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.control.NonFatal

case class RequireAnyNewerRole(
    keyForGetMap: ResourceId,
    getMapFunc: (ResourceId) => Future[Either[Error, TypedIdRoleAndUpdateAtMap]]
) extends ProfileAuthorizer
    with Logging {
  override def isAuthorized(
      webContext: WebContext,
      sessionStore: SessionStore,
      profiles: java.util.List[UserProfile]
  ): Boolean = {
    isAnyAuthorized(webContext, sessionStore, profiles)
  }

  override def isProfileAuthorized(
      webContext: WebContext,
      sessionStore: SessionStore,
      profile: UserProfile
  ): Boolean = {
    profile.isInstanceOf[OidcProfile] match {
      case true =>
        RequireAnyNewerRole.isProfileSignInAfterUpdate(
          profile.asInstanceOf[OidcProfile],
          getMapFunc(keyForGetMap)
        )

      case false => {
        logger.debug("profile is not InstanceOf oidc.profile.OidcProfile.")
        false
      }
    }
  }
}

object RequireAnyNewerRole extends Logging {
  def isProfileSignInAfterUpdate(
      profile: OidcProfile,
      allowedMap: Future[Either[Error, TypedIdRoleAndUpdateAtMap]]
  ): Boolean = {
    val profileSignInDate = profile.getIssueAtAsInstant()
    val profileId: TypedId = profile.getTypedId()
    val r =
      try { Await.result(allowedMap, 3.seconds) }
      catch { case NonFatal(e) => Left(AwaitError(e)) }

    r match {
      case Right(map) => {
        map.get(profileId) match {
          case Some(roleAndUpdateAt) =>
            roleAndUpdateAt.isOlder(profileSignInDate)
          case None => false
        }
      }
      case Left(error) => {
        logger.debug(s"error occur ${error}")
        false
      }
    }
  }

  // factory method filtering allowedMap: (key, roleAndUpdate) -> roleAndUpdate include roleObject
  def Of(roleObject: Role)(
      keyForGetMap: ResourceId,
      getMapFunc: (
          ResourceId
      ) => Future[Either[Error, TypedIdRoleAndUpdateAtMap]]
  )(implicit ec: ExecutionContext): RequireAnyNewerRole = {

    RequireAnyNewerRole(
      keyForGetMap,
      (ResourceId) =>
        getMapFunc(keyForGetMap)
          .map(
            _.map(
              _.filter {
                case (_, RoleAndUpdateAt(role, _)) => {
                  role equals roleObject
                }
              }
            )
          )
    )
  }
}
