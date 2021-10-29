package authorization.repository

import authorization._
import javax.inject.Inject
import play.api.cache._
import play.api.Logging
import scala.concurrent.Await
import scala.concurrent.duration._

class AuthorityRepositoryWithCache @Inject() (cache: AsyncCacheApi)
    extends AuthorityRepository
    with Logging {
  implicit val ec = scala.concurrent.ExecutionContext.global

  val cacheKey = "test"
  override def getTypedIdRoleAndUpdateAtMap(
      resourceId: ResourceId
  ): TypedIdRoleAndUpdateAtMap = {
    val f = cache
      .get[TypedIdRoleAndUpdateAtMap](resourceId)

    logger.info("read cache")
    Await.result(f, 5.seconds).getOrElse(Map.empty)
  }
}
