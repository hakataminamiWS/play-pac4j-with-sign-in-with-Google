package modules

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import authorization.repository.AuthorityRepository
import authorization.repository.AuthorityRepositoryWithCache
import authorization.repository.AuthorityRepositoryWithDB

class Module extends AbstractModule {
  override def configure() = {
    // authorityRepository
    bind(classOf[AuthorityRepository])
      .annotatedWith(Names.named("repoCache"))
      .to(classOf[AuthorityRepositoryWithCache])
    bind(classOf[AuthorityRepository])
      .annotatedWith(Names.named("repoDB"))
      .to(classOf[AuthorityRepositoryWithDB])
  }
}
