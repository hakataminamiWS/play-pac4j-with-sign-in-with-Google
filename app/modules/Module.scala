package modules

import com.google.inject.AbstractModule
import authorization.repository.AuthorityRepository
import authorization.repository.AuthorityRepositoryWithCache

class Module extends AbstractModule {
  override def configure() = {
    // authorityRepository
    bind(classOf[AuthorityRepository]).to(classOf[AuthorityRepositoryWithCache])
  }
}
