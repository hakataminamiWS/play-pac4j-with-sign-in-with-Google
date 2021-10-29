package authorization.repository

import authorization._

trait AuthorityRepository {
  def getTypedIdRoleAndUpdateAtMap: ResourceId => TypedIdRoleAndUpdateAtMap
}
