package authorization.roles

import java.time.format.DateTimeParseException
import java.time.Instant

sealed trait Role
case object Owner extends Role
case object Contributor extends Role

case class RoleAndUpdateAt(role: Role, val updateAt: Instant) {
  def isOlder(instant: Instant): Boolean = {
    this.updateAt isBefore instant
  }

  def isOlder(optValue: Option[_]): Boolean = {
    optValue match {
      case Some(str: String)      => isOlder(str)
      case Some(instant: Instant) => isOlder(instant)
      case Some(_)                => false
      case None                   => false
    }
  }

  def isOlder(roleAndUpdateAt: RoleAndUpdateAt): Boolean = {
    isOlder(roleAndUpdateAt.updateAt)
  }

  def isOlder(maybeParseable: String): Boolean = {
    val optInstant =
      try { Some(Instant.parse(maybeParseable)) }
      catch {
        case e: DateTimeParseException => None
      }
    optInstant match {
      case None    => false
      case Some(i) => isOlder(i)
    }
  }
}
