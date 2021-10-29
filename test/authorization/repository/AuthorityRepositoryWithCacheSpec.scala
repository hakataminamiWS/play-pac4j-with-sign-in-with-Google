package authorization.repository

import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.scalatestplus.play.PlaySpec

class AuthorityRepositoryWithCacheSpec
    extends PlaySpec
    with GuiceOneAppPerTest {
  "test" should {
    "test" in {
      val test = app.injector.instanceOf[AuthorityRepositoryWithCache]
      test.getTypedIdRoleAndUpdateAtMap("test")
    }
  }

}
