package org.apized.auth;

import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import org.apized.auth.api.role.Role;
import org.apized.auth.api.role.RoleRepository;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserRepository;
import org.apized.micronaut.server.ApizedStartupEvent;

import java.util.List;
import java.util.Optional;

@Singleton
public class Initializer {

  @Inject
  UserRepository userRepository;

  @Inject
  RoleRepository roleRepository;

  @Value("${auth.domain}")
  String domain;

  @Getter
  private Role defaultRole;

  @EventListener
  void onStartup(ApizedStartupEvent event) {
    ensureDefaultRole();
    ensureAdministrator();
  }

  private void ensureDefaultRole() {
    defaultRole = roleRepository.findDefaultRole().or(() ->
      {
        Role role = new Role(
          "Default",
          "The default role contains the permissions any user should get, including anonymous access",
          List.of("auth.user.create"),
          List.of()
        );
        role.getMetadata().put("default", true);
        return Optional.ofNullable(roleRepository.create(role));
      }
    ).get();
  }

  private void ensureAdministrator() {
    String username = String.format("administrator@%s", domain);
    userRepository.findByUsername(username).or(() ->
      Optional.ofNullable(userRepository.create(new User(
        username,
        "Administrator",
        BCrypt.hashpw("changeme", BCrypt.gensalt()),
        true,
        List.of("*"),
        List.of(),
        null,
        null)
      )));
  }
}
