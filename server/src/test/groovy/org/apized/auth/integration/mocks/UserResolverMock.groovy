package org.apized.auth.integration.mocks

import io.micronaut.context.annotation.Replaces
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import org.apized.auth.api.role.RoleService
import org.apized.auth.api.user.UserRepository
import org.apized.auth.security.AuthConverter
import org.apized.auth.security.AuthStartupEvent
import org.apized.auth.security.DBUserResolver
import org.apized.core.StringHelper
import org.apized.core.security.model.User
import org.apized.micronaut.test.integration.mocks.AbstractMicronautUserResolverMock

@Singleton
@Replaces(DBUserResolver)
class UserResolverMock extends AbstractMicronautUserResolverMock {
  private UserRepository userRepository
  private RoleService roleService

  UserResolverMock(UserRepository userRepository, RoleService roleService) {
    this.userRepository = userRepository
    this.roleService = roleService
  }

  @EventListener
  void onStartup(AuthStartupEvent event) {
    userRepository.findAll().each {
      registerUser(it)
    }
  }

  @Override
  Map<String, User> getKnownUsers() {
    [ : ] as Map<String, User>
  }

  @Override
  User getUser(String token) {
    if (token != null) {
      Optional<org.apized.auth.api.user.User> user = userRepository.get(StringHelper.convertStringToUUID(token))
      if (user.isPresent()) {
        return AuthConverter.convertAuthUserToApizedUser(
          user.get()
        )
      }
    }
    Optional.ofNullable(
      users.get(token)
    ).orElse(
      new User(
        id: token ? UUID.fromString(token) : UUID.randomUUID(),
        name: "Anonymous",
        username: "anonymous@apized.com",
        roles: [ AuthConverter.convertAuthRoleToApizedRole(roleService.findDefaultRole().get()) ]
      )
    )
  }

  void registerUser(org.apized.auth.api.user.User it) {
    User user = AuthConverter.convertAuthUserToApizedUser(it)
    userAlias[it.username.replaceAll(/@.*/, '')] = user
    users[it.id] = user
  }
}
