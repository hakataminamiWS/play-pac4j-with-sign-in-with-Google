package oidc.client

import com.nimbusds.jose.JWSAlgorithm
import oidc.profile.LineOidcProfile
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.oidc.profile.creator.OidcProfileCreator
import org.pac4j.oidc.profile.OidcProfile
import org.pac4j.oidc.profile.OidcProfileDefinition

class LineOidcClient(oidcConfiguration: OidcConfiguration)
    extends OidcClient(oidcConfiguration) {
  override def internalInit(): Unit = {
    getConfiguration().defaultDiscoveryURI(
      "https://access.line.me/.well-known/openid-configuration"
    )
    val profileCreator = new OidcProfileCreator(getConfiguration(), this)
    profileCreator.setProfileDefinition(
      new OidcProfileDefinition(x => new LineOidcProfile())
    )
    defaultProfileCreator(profileCreator)

    super.internalInit()
  }
}