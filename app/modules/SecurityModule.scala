package modules

import play.api.{Configuration, Environment}
import com.google.inject.{AbstractModule, Provides}

import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.client.Clients
import org.pac4j.core.config.Config
import org.pac4j.core.context.session.SessionStore

import org.pac4j.play.CallbackController
import org.pac4j.play.LogoutController
import org.pac4j.play.http.PlayHttpActionAdapter
import org.pac4j.play.scala.DefaultSecurityComponents
import org.pac4j.play.scala.SecurityComponents
import org.pac4j.play.store.PlayCacheSessionStore

import org.pac4j.oidc.client.GoogleOidcClient
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.OidcConfiguration

class SecurityModule(environment: Environment, configuration: Configuration)
    extends AbstractModule {

  val baseUrl: String = configuration.get[String]("pac4j.baseUrl")

  override def configure(): Unit = {
    bind(classOf[SessionStore]).to(classOf[PlayCacheSessionStore])
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

    val googleOidcClient = new GoogleOidcClient(oidcConfig)
    googleOidcClient
  }

  @Provides
  def provideConfig(googleOidcClient: GoogleOidcClient): Config = {
    val clients = new Clients(baseUrl + "/callback", googleOidcClient)

    val config = new Config(clients)
    config.setHttpActionAdapter(new PlayHttpActionAdapter())
    config
  }
}
