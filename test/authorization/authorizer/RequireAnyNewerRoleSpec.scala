package authorization.authorizer

import authorization.roles.Contributor
import authorization.roles.Owner
import authorization.roles.RoleAndUpdateAt
import authorization.TypedIdRoleAndUpdateAtMap
import java.time.Instant
import java.util.Date
import oidc.profile.OidcProfile
import org.scalatestplus.play.PlaySpec
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

class RequireAnyNewerRoleSpec extends PlaySpec {
  val olderJavaTimeInstant = Instant.parse("2000-01-01T01:02:03.456789Z")
  val newerJavaTimeInstant = Instant.parse("2111-01-01T01:02:03.456789Z")

  val profile = new OidcProfile()
  profile.setId("testId")
  profile.addAttribute("iat", Date.from(newerJavaTimeInstant))

  val testTypedId = profile.getTypedId()
  val profiles = List[OidcProfile](profile).asJava

  "RequireAnyNewerRole" should {
    "return false, profile's TypedId is Not included in allowedTypeIdRoleAndUpdateMap's key" in {
      val notIncludedAllowedMap =
        Map(
          testTypedId + "not included" -> RoleAndUpdateAt(
            Owner,
            olderJavaTimeInstant
          )
        )

      val resultOfNotIncluded =
        RequireAnyNewerRole(
          "test",
          (test) => Future.successful(Right((notIncludedAllowedMap)))
        )
          .isProfileAuthorized(
            null,
            null,
            profile
          )

      resultOfNotIncluded mustBe (false)
    }

    "return false, profile's TypedId is included in allowedTypeIdRoleAndUpdateMap's key" +
      " but profiles's iat is older than (or equal to ) the allowedTypeIdRoleAndUpdateMap's value" in {
        val newerThanUpdateAtOfProfileAllowedMap =
          Map(
            testTypedId -> RoleAndUpdateAt(
              Owner,
              newerJavaTimeInstant plusSeconds (1L)
            )
          )
        val equalToUpdateAtOfProfileAllowedMap =
          Map(
            testTypedId -> RoleAndUpdateAt(
              Owner,
              newerJavaTimeInstant
            )
          )

        val resultOfNewerThan = RequireAnyNewerRole(
          "test",
          (test) =>
            Future.successful(Right(newerThanUpdateAtOfProfileAllowedMap))
        ).isProfileAuthorized(
          null,
          null,
          profile
        )
        val resultOfEqualTo = RequireAnyNewerRole(
          "test",
          (test) => Future.successful(Right(equalToUpdateAtOfProfileAllowedMap))
        ).isProfileAuthorized(
          null,
          null,
          profile
        )

        resultOfNewerThan mustBe (false)
        resultOfEqualTo mustBe (false)
      }

    "return true, profile's TypedId is included in allowedTypeIdRoleAndUpdateMap's key" +
      " and profiles's iat is newer than the allowedTypeIdRoleAndUpdateMap's value" in {
        val allowedMap: TypedIdRoleAndUpdateAtMap =
          Map(testTypedId -> RoleAndUpdateAt(Owner, olderJavaTimeInstant))

        val result =
          RequireAnyNewerRole(
            "test",
            (test) => Future.successful(Right(allowedMap))
          ).isProfileAuthorized(
            null,
            null,
            profile
          )

        result mustBe (true)
      }
  }

  "RequireAnyNewerRole.Of(Owner)" should {
    "return false, if allowedMap does not include Contributor RoleAndUpdate." in {
      implicit val ec = scala.concurrent.ExecutionContext.global

      val notIncludeOwnerAllowedMap =
        Map(testTypedId -> RoleAndUpdateAt(Contributor, olderJavaTimeInstant))

      val result = RequireAnyNewerRole
        .Of(Owner)(
          "test",
          (test) => Future.successful(Right(notIncludeOwnerAllowedMap))
        )
        .isProfileAuthorized(null, null, profile)

      result mustBe (false)
    }

    "return true, if allowedMap includes Owner RoleAndUpdate." in {
      implicit val ec = scala.concurrent.ExecutionContext.global

      val includeOwnerAllowedMap =
        Map(testTypedId -> RoleAndUpdateAt(Owner, olderJavaTimeInstant))

      val result = RequireAnyNewerRole
        .Of(Owner)(
          "test",
          (test) => Future.successful(Right(includeOwnerAllowedMap))
        )
        .isProfileAuthorized(null, null, profile)

      result mustBe (true)
    }
  }
}
