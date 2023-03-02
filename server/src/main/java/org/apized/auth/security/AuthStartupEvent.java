package org.apized.auth.security;

import io.micronaut.context.event.ApplicationEvent;
import org.apized.core.ApizedConfig;

public class AuthStartupEvent extends ApplicationEvent {
  public AuthStartupEvent() {
    super(ApizedConfig.getInstance());
  }
}

