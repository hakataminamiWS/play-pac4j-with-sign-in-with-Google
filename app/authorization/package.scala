import authorization.roles.RoleAndUpdateAt

package object authorization {

  type ResourceId = String

  type TypedId = String
  type TypedIdRoleAndUpdateAtMap = Map[TypedId, RoleAndUpdateAt]
}
