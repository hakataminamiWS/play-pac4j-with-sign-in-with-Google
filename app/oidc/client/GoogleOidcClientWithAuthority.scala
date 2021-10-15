package oidc.client

import org.pac4j.oidc.client.GoogleOidcClient
import org.pac4j.oidc.config.OidcConfiguration

class GoogleOidcClientWithAuthority(oidcConfiguration: OidcConfiguration)
    extends GoogleOidcClient(oidcConfiguration) {}
