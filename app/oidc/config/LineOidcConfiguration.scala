package oidc.config

import com.nimbusds.jose.JWSAlgorithm
import org.pac4j.oidc.config.OidcConfiguration

// Directly add jwsAlgorithm,
// Because LINE only support HS256 jwsAlgorithm for id token, and pac4j configuration do not read id token header's alg
class LineOidcConfiguration extends OidcConfiguration {

  def setIDTokenJwsAlgorithm(jwsAlgorithm: JWSAlgorithm): Unit = {
    val meta = this.findProviderMetadata()
    meta.getIDTokenJWSAlgs().add(jwsAlgorithm)
    this.setProviderMetadata(meta)
  }

  def setIDTokenJwsAlgorithm(jwsAlgorithmString: String): Unit = {
    val meta = this.findProviderMetadata()
    meta.getIDTokenJWSAlgs().add(JWSAlgorithm.parse(jwsAlgorithmString))
    this.setProviderMetadata(meta)
  }
}
