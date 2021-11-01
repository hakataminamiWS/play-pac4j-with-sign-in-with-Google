package authorization.repository

import authorization._
import javax.inject.Inject
import play.api.cache._
import play.api.Logging
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import javax.inject.Named

class AuthorityRepositoryWithCache @Inject() (
    @Named("repoDB") dbRepository: AuthorityRepository,
    cache: AsyncCacheApi,
    implicit val ec: ExecutionContext
) extends AuthorityRepository
    with Logging {

  override def getTypedIdRoleAndUpdateAtMap
      : ResourceId => Future[TypedIdRoleAndUpdateAtMap] =
    (resourceId: ResourceId) => {
      val cacheKey = resourceId

      val f = cache
        .get[TypedIdRoleAndUpdateAtMap](cacheKey)

      f.flatMap(_ match {
        case None => {
          val f = dbRepository.getTypedIdRoleAndUpdateAtMap(resourceId)
          f.map { t =>
            logger.info(s"cache set, key: ${cacheKey}, value: ${t}")
            cache.set(cacheKey, t)
            t
          }
        }
        case Some(t) => Future(t)
      })
    }
}
