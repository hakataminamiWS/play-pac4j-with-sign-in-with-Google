package authorization.roles

import java.time.Instant
import org.scalatestplus.play.PlaySpec

class RolesSpec extends PlaySpec {
  val olderParseableString = "2000-01-01T01:02:03.456789Z"
  val newerParseableString = "2111-01-01T01:02:03.456789Z"

  val olderJavaTimeInstant = Instant.parse(olderParseableString)
  val newerJavaTimeInstant = Instant.parse(newerParseableString)

  "The isOlder(instant: Instant) method of a instance of RoleAndUpdateAt" should {
    "return true when the instant is older, false when equal or newer." in {

      val older = RoleAndUpdateAt(Owner, olderJavaTimeInstant)
      val newer = RoleAndUpdateAt(Contributor, newerJavaTimeInstant)

      older.isOlder(newerJavaTimeInstant) mustBe true

      older.isOlder(olderJavaTimeInstant) mustBe false
      newer.isOlder(olderJavaTimeInstant) mustBe false
    }
  }

  "The isOlder(optValue: Option[_]) method of a instance of RoleAndUpdateAt" should {
    "return false when optValue is None." in {

      val instance = RoleAndUpdateAt(Owner, olderJavaTimeInstant)

      instance.isOlder(None) mustBe false
    }
  }

  "The isOlder(roleAndUpdateAt: RoleAndUpdateAt) method of a instance of RoleAndUpdateAt" should {
    "return true when the instant is older, false when equal or newer." in {

      val older = RoleAndUpdateAt(Owner, olderJavaTimeInstant)
      val newer = RoleAndUpdateAt(Contributor, newerJavaTimeInstant)

      older.isOlder(newer) mustBe true

      older.isOlder(older) mustBe false
      newer.isOlder(older) mustBe false
    }
  }

  "The isOlder(maybeParseable: String) method of a instance of RoleAndUpdateAt" should {
    "return false when maybeParseable string is not parsed to a Java time Instant." in {

      val instance = RoleAndUpdateAt(Owner, olderJavaTimeInstant)
      val notParseableString = olderParseableString + "not parseable"

      instance.isOlder(notParseableString) mustBe false
    }
  }

}
