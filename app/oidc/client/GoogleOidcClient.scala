package oidc.client

import oidc.profile.GoogleOidcProfile
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.oidc.profile.creator.OidcProfileCreator
import org.pac4j.oidc.profile.OidcProfile
import org.pac4j.oidc.profile.OidcProfileDefinition

class GoogleOidcClient(oidcConfiguration: OidcConfiguration)
    extends OidcClient(oidcConfiguration) {

  override def internalInit(): Unit = {
    getConfiguration().defaultDiscoveryURI(
      "https://accounts.google.com/.well-known/openid-configuration"
    )
    val profileCreator = new OidcProfileCreator(getConfiguration(), this)
    profileCreator.setProfileDefinition(
      new OidcProfileDefinition(x => new GoogleOidcProfile())
    )
    defaultProfileCreator(profileCreator)

    super.internalInit()
  }

}
