package authorization.repository

import authorization._
import scala.concurrent.Future

sealed trait Error

final case class DBError(message: String) extends Error
final case class AwaitError(e: Throwable) extends Error

trait AuthorityRepository {
  def getTypedIdRoleAndUpdateAtMap
      : ResourceId => Future[Either[Error, TypedIdRoleAndUpdateAtMap]]
}
