package authorization.repository

import authorization.ResourceId
import authorization.TypedIdRoleAndUpdateAtMap
import controllers.DataSetForDemo
import com.google.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

class AuthorityRepositoryWithDB @Inject() (
    dataSet: DataSetForDemo,
    implicit val ec: ExecutionContext
) extends AuthorityRepository {

  override def getTypedIdRoleAndUpdateAtMap
      : ResourceId => Future[TypedIdRoleAndUpdateAtMap] =
    (resourceId: ResourceId) => {
      Thread.sleep(3000)
      Future(dataSet.allowedMap)
      //   Future(Map.empty)
    }
}
