package authorization.repository

import controllers.DataSetForDemo

import authorization.ResourceId
import authorization.TypedIdRoleAndUpdateAtMap
import com.google.inject.Inject
import play.api.Logging
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class AuthorityRepositoryWithDB @Inject() (dataSet: DataSetForDemo)(implicit
    ec: ExecutionContext
) extends AuthorityRepository
    with Logging {

  override def getTypedIdRoleAndUpdateAtMap
      : ResourceId => Future[Either[Error, TypedIdRoleAndUpdateAtMap]] =
    (resourceId: ResourceId) => {
      Future.successful(Right(dataSet.allowedMap))
      // Future.successful(Left(DBError("no value")))
    }
}
