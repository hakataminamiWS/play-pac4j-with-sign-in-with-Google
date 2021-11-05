import oidc.profile.OidcProfile
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.profile.ProfileManager
import org.pac4j.play.PlayWebContext
import play.api.mvc.RequestHeader
import scala.jdk.OptionConverters._

package object controllers {

  def getProfile(request: RequestHeader)(implicit
      store: SessionStore
  ): Option[OidcProfile] = {
    val context = new PlayWebContext(request)
    val profileManager =
      new ProfileManager(context, store)
    val profile = profileManager.getProfile(classOf[OidcProfile])
    profile.toScala
  }
}
