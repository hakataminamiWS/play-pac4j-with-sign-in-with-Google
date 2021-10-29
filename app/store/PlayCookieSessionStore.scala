package store

import java.util.LinkedHashMap
import javax.inject.Singleton
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.jwt.JwtClaims
import org.pac4j.oidc.profile.OidcProfileDefinition
import org.pac4j.play.store.{PlayCookieSessionStore => pac4jPlayCookieSessionStore}
import org.pac4j.play.store.DataEncrypter
import play.api.Logging
import scala.jdk.CollectionConverters._

/** for clear non used profile attributes,
  * sign in override clearUserProfiles of PlayCookieSessionStore.
  */
@Singleton
class PlayCookieSessionStore(dataEncrypter: DataEncrypter)
    extends pac4jPlayCookieSessionStore(dataEncrypter)
    with Logging {

  override def clearUserProfiles(value: Object): Object = {
    val profiles = super
      .clearUserProfiles(value)
      .asInstanceOf[LinkedHashMap[String, CommonProfile]]
    profiles.forEach((name, profile) => {
      val removedAttributes = profile.getAttributes().asScala
      // OidcProfile's name, picture, and Jwt Claim's iat are not remove.
      removedAttributes -= OidcProfileDefinition.NAME
      removedAttributes -= OidcProfileDefinition.PICTURE
      removedAttributes -= JwtClaims.ISSUED_AT
      //
      removedAttributes.keys.foreach { key =>
        logger.debug(s"Remove attribute: ${key}")
        profile.removeAttribute(key)
      }
    })
    return profiles
  }
}
