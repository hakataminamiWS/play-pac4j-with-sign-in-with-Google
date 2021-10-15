package store

import java.util.LinkedHashMap
import javax.inject.Singleton
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.util.serializer.JavaSerializer
import org.pac4j.oidc.profile.OidcProfileDefinition
import org.pac4j.play.store.DataEncrypter
import org.pac4j.play.store.PlayCookieSessionStore
import play.api.Logging
import scala.jdk.CollectionConverters._

/** for clear non used profile attributes, override clearUserProfiles of
  * PlayCookieSessionStore.
  */
@Singleton
class ModPlayCookieSessionStore(dataEncrypter: DataEncrypter)
    extends PlayCookieSessionStore(dataEncrypter)
    with Logging {

  override def clearUserProfiles(value: Object): Object = {
    val profiles = super
      .clearUserProfiles(value)
      .asInstanceOf[LinkedHashMap[String, CommonProfile]]
    profiles.forEach((name, profile) => {
      val removedAttributes = profile.getAttributes().asScala
      // remain OidcProfile's name, picture
      removedAttributes -= OidcProfileDefinition.NAME
      removedAttributes -= OidcProfileDefinition.PICTURE
      removedAttributes.keys.foreach { key =>
        logger.debug(s"Remove attribute: ${key}")
        profile.removeAttribute(key)
      }
    })
    return profiles
  }
}