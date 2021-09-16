package oidc.config;

import org.pac4j.oidc.config.OidcConfiguration;
import com.nimbusds.jose.JWSAlgorithm;

// Directly add jwsAlgorithm, 
// Because LINE only support HS256 jwsAlgorithm for id token, and pac4j configuration do not read id token header's alg 
public class LineOidcConfiguration extends OidcConfiguration {
    public void setIDTokenJwsAlgorithm(final JWSAlgorithm jwsAlgorithm) {
        var meta = this.findProviderMetadata();
        meta.getIDTokenJWSAlgs().add(jwsAlgorithm);
        this.setProviderMetadata(meta);
    }

    public void setIDTokenJwsAlgorithm(final String jwsAlgorithm) {
        var meta = this.findProviderMetadata();
        meta.getIDTokenJWSAlgs().add(JWSAlgorithm.parse(jwsAlgorithm));
        this.setProviderMetadata(meta);
    }
}