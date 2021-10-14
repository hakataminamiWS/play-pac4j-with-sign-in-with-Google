package oidc.client;

import org.pac4j.oidc.client.GoogleOidcClient;
import org.pac4j.oidc.config.OidcConfiguration;

public class GoogleOidcClientWithAuthority extends GoogleOidcClient {

    public GoogleOidcClientWithAuthority() {
    }

    public GoogleOidcClientWithAuthority(final OidcConfiguration configuration) {
        super(configuration);
    }
}