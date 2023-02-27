package org.apized.auth.integration.mocks

import io.micronaut.context.annotation.Replaces
import jakarta.inject.Singleton
import org.apized.auth.DBUserResolver
import org.apized.core.security.model.User
import org.apized.micronaut.test.integration.mocks.AbstractMicronautUserResolverMock

@Singleton
@Replaces(DBUserResolver)
class UserResolverMock extends AbstractMicronautUserResolverMock {

  @Override
  Map<UUID, User> getKnownUsers() {
    [
      admin    : new User(username: 'admin@websummit.com', permissions: [ '*' ]),
      anonymous: new User(username: 'anonymous@test.com', permissions: [ ])
    ] as Map<UUID, User>
  }
}
