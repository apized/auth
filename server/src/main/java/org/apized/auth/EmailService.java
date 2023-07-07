package org.apized.auth;

import java.util.Map;

public interface EmailService {
  void send(String templateId, String name, String email, Map<String, Object> variables);
}
