package oidc.client;

import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.profile.OidcProfileDefinition;
import org.pac4j.oidc.profile.OidcProfile;
import org.pac4j.oidc.profile.creator.OidcProfileCreator;

import org.pac4j.oidc.client.OidcClient;
import com.nimbusds.jose.JWSAlgorithm;

import oidc.profile.LineOidcProfile;

/**
 * <p>This class is the OpenID Connect client to authenticate users in Line.</p>
 * <p>More information at: https://developers.line.biz/ja/docs/line-login/integrate-line-login/#making-an-authorization-request</p>
 *
 */
public class LineOidcClient extends OidcClient {

    public LineOidcClient() {
    }

    public LineOidcClient(final OidcConfiguration configuration) {
        super(configuration);
        // configuration.setIDTokenJWSAlgs(JWSAlgorithm.HS256);
    }

    @Override
    protected void internalInit() {
        getConfiguration().defaultDiscoveryURI("https://access.line.me/.well-known/openid-configuration");
        final var profileCreator = new OidcProfileCreator(getConfiguration(), this);
        profileCreator.setProfileDefinition(new OidcProfileDefinition(x -> new LineOidcProfile()));
        defaultProfileCreator(profileCreator);

        super.internalInit();
    }
}
