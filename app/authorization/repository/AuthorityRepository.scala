package authorization.repository

import authorization._
import scala.concurrent.Future

trait AuthorityRepository {
  def getTypedIdRoleAndUpdateAtMap
      : ResourceId => Future[TypedIdRoleAndUpdateAtMap]
}
