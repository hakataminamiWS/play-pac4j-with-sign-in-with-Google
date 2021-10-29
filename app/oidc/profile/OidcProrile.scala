package oidc.profile

import java.time.Instant
import org.pac4j.core.profile.jwt.JwtClaims
import org.pac4j.oidc.profile.{OidcProfile => pac4jOidcProfile}
import play.api.Logger

/** for get issueAt claim as java time instant,
 *  add a method.
  */

class OidcProfile extends pac4jOidcProfile {
  val log: Logger = Logger(this.getClass())

  def getIssueAtAsInstant(): Option[Instant] = {
    val optDate = Option(getAttributeAsDate(JwtClaims.ISSUED_AT))

    optDate match {
      case Some(date) => Some(date.toInstant())
      case None => {
        log.debug(s"can not getIssueAtAsInstant, get null.")
        None
      }
    }
  }
}
