package modules

import com.google.inject.{AbstractModule, Provides}
import com.nimbusds.jose.JWSAlgorithm
import java.nio.charset.StandardCharsets
import oidc.client.GoogleOidcClientWithAuthority
import oidc.client.LineOidcClient
import oidc.config.LineOidcConfiguration
import org.pac4j.core.authorization.authorizer.RequireAnyPermissionAuthorizer
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.authorization.generator.DefaultRolesPermissionsAuthorizationGenerator
import org.pac4j.core.client.Clients
import org.pac4j.core.config.Config
import org.pac4j.core.context.session.SessionStore
import org.pac4j.oidc.client.GoogleOidcClient
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.play.CallbackController
import org.pac4j.play.http.PlayHttpActionAdapter
import org.pac4j.play.LogoutController
import org.pac4j.play.scala.DefaultSecurityComponents
import org.pac4j.play.scala.SecurityComponents
import org.pac4j.play.store.PlayCacheSessionStore
import org.pac4j.play.store.PlayCookieSessionStore
import org.pac4j.play.store.ShiroAesDataEncrypter
import play.api.{Configuration, Environment}
import store.ModPlayCookieSessionStore

class SecurityModule(environment: Environment, configuration: Configuration)
    extends AbstractModule {

  val baseUrl: String = configuration.get[String]("pac4j.baseUrl")

  override def configure(): Unit = {
    // for serialize custom profile
    PlayCookieSessionStore.JAVA_SERIALIZER.addTrustedClass(
      classOf[oidc.profile.LineOidcProfile]
    )

    // sessionStore, SecurityComponents
    val sKey = configuration
      .get[String]("pac4j.play.selialiser.secret.key")
      .substring(0, 16)
    val dataEncrypter = new ShiroAesDataEncrypter(
      sKey.getBytes(StandardCharsets.UTF_8)
    )
    val modPlaySessionStore = new ModPlayCookieSessionStore(dataEncrypter)
    bind(classOf[SessionStore]).toInstance(modPlaySessionStore)

    bind(classOf[SecurityComponents]).to(classOf[DefaultSecurityComponents])

    // callback
    val callbackController = new CallbackController()
    callbackController.setDefaultUrl("/") // default url after sign out
    bind(classOf[CallbackController]).toInstance(callbackController)

    // logout
    val logoutController = new LogoutController()
    logoutController.setDefaultUrl("/")
    bind(classOf[LogoutController]).toInstance(logoutController)
  }

  @Provides
  def provideGoogleOidcClient: GoogleOidcClient = {
    val googleOidcClient = new GoogleOidcClient(googleOidcConfigSetup)
    googleOidcClient.setMultiProfile(true)
    googleOidcClient
  }
  @Provides
  def provideGoogleOidcClientWithAuthority: GoogleOidcClientWithAuthority = {
    val googleOidcClientWithAuthority = new GoogleOidcClientWithAuthority(
      googleOidcConfigSetup
    )
    googleOidcClientWithAuthority.setMultiProfile(true)
    googleOidcClientWithAuthority.addAuthorizationGenerator(
      new DefaultRolesPermissionsAuthorizationGenerator(
        Array.empty[String], // defaultRoles
        Array("test") // defaultPermissions
      )
    )
    googleOidcClientWithAuthority
  }
  private def googleOidcConfigSetup: OidcConfiguration = {
    val oidcConfig = new OidcConfiguration()
    oidcConfig.setClientId(configuration.get[String]("pac4j.google.clientID"))
    oidcConfig.setSecret(configuration.get[String]("pac4j.google.clientSecret"))
    oidcConfig.setDiscoveryURI(
      configuration.get[String]("pac4j.google.discoveryURI")
    )
    oidcConfig.addCustomParam("prompt", "consent")
    oidcConfig.setResponseType("code")
    oidcConfig.setResponseMode("query")
    oidcConfig.setScope("openid email profile")
    oidcConfig.setUseNonce(true)
    oidcConfig.setWithState(true)
    oidcConfig
  }

  @Provides
  def provideLineOidcClient: LineOidcClient = {
    val lineOidcClient = new LineOidcClient(lineOidcConfigSetup)
    lineOidcClient.setMultiProfile(true)
    lineOidcClient
  }
  private def lineOidcConfigSetup: LineOidcConfiguration = {
    val lineOidcConfig = new LineOidcConfiguration()
    lineOidcConfig.setClientId(
      configuration.get[String]("pac4j.line.channelID")
    )
    lineOidcConfig.setSecret(
      configuration.get[String]("pac4j.line.channelSecret")
    )
    lineOidcConfig.setDiscoveryURI(
      configuration.get[String]("pac4j.line.discoveryURI")
    )
    lineOidcConfig.addCustomParam("prompt", "consent")
    lineOidcConfig.setResponseType("code")
    lineOidcConfig.setScope("profile openid")
    lineOidcConfig.setUseNonce(true)
    lineOidcConfig.setWithState(true)
    lineOidcConfig.setIDTokenJwsAlgorithm(JWSAlgorithm.HS256)
    lineOidcConfig
  }

  @Provides
  def provideConfig(
      googleOidcClient: GoogleOidcClient,
      lineOidcClient: LineOidcClient,
      googleOidcClientWithAuthority: GoogleOidcClientWithAuthority
  ): Config = {
    val clients =
      new Clients(
        baseUrl + "/callback",
        googleOidcClient,
        lineOidcClient,
        googleOidcClientWithAuthority
      )

    val config = new Config(clients)
    config.setHttpActionAdapter(new PlayHttpActionAdapter())
    config.addAuthorizer("test", new RequireAnyPermissionAuthorizer("test"))
    config
  }
}
