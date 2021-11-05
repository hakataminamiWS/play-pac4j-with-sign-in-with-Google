package authorization.repository

import authorization._
import javax.inject.Inject
import javax.inject.Named
import play.api.cache._
import play.api.Logging
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class AuthorityRepositoryWithCache @Inject() (
    @Named("repoDB") dbRepository: AuthorityRepository,
    cache: AsyncCacheApi
)(implicit ec: ExecutionContext)
    extends AuthorityRepository
    with Logging {

  override def getTypedIdRoleAndUpdateAtMap
      : ResourceId => Future[Either[Error, TypedIdRoleAndUpdateAtMap]] =
    (resourceId: ResourceId) => {
      val cacheKey = resourceId
      cache
        .get[TypedIdRoleAndUpdateAtMap](cacheKey)
        .flatMap {
          case None =>
            dbRepository
              .getTypedIdRoleAndUpdateAtMap(resourceId)
              .map(_.map { map =>
                logger.debug(s"cache set, key: ${cacheKey}, value: ${map}")
                cache.set(cacheKey, map)
                map
              })

          case Some(t) => Future(Right(t))
        }
    }
}
