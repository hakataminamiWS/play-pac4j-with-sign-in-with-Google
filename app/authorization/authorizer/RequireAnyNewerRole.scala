package authorization.authorizer

import authorization.roles.Role
import authorization.roles.RoleAndUpdateAt
import authorization._
import oidc.profile.OidcProfile
import org.pac4j.core.authorization.authorizer.ProfileAuthorizer
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.UserProfile
import play.api.Logging

class RequireAnyNewerRole(
    allowedTypeIdRoleAndUpdateMap: => TypedIdRoleAndUpdateAtMap
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
          allowedTypeIdRoleAndUpdateMap
        )

      case false => {
        logger.debug("profile is not InstanceOf oidc.profile.OidcProfile.")
        false
      }
    }
  }
}

object RequireAnyNewerRole extends {
  private def isProfileSignInAfterUpdate(
      profile: OidcProfile,
      allowedMap: TypedIdRoleAndUpdateAtMap
  ): Boolean = {
    val profileSignInDate = profile.getIssueAtAsInstant()

    val profileId: TypedId = profile.getTypedId()
    allowedMap.get(profileId) match {
      case Some(roleAndUpdateAt) => roleAndUpdateAt.isOlder(profileSignInDate)
      case None                  => false
    }
  }

  // factory method filtering allowedMap: (key, roleAndUpdate) -> roleAndUpdate include roleObject
  def Of(roleObject: Role)(
      allowedTypeIdRoleAndUpdateMap: => TypedIdRoleAndUpdateAtMap
  ): RequireAnyNewerRole = {
    new RequireAnyNewerRole(
      allowedTypeIdRoleAndUpdateMap.filter {
        case (_, RoleAndUpdateAt(role, _)) =>
          role equals roleObject
      }
    )
  }
}
