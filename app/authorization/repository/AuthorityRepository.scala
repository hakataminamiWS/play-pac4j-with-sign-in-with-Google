package authorization.repository

import authorization._

trait AuthorityRepository {
  def getTypedIdRoleAndUpdateAtMap(
      resourceId: ResourceId
  ): TypedIdRoleAndUpdateAtMap
}
