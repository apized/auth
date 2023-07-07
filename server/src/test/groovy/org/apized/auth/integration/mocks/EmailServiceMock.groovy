package org.apized.auth.integration.mocks

import io.micronaut.context.annotation.Replaces
import io.micronaut.serde.ObjectMapper
import jakarta.inject.Singleton
import org.apized.auth.EmailService
import org.apized.auth.SendgridEmailService
import org.apized.test.integration.service.AbstractServiceIntegrationMock

@Singleton
@Replaces(SendgridEmailService.class)
class EmailServiceMock extends AbstractServiceIntegrationMock implements EmailService {
  EmailServiceMock(ObjectMapper mapper) {
    super(mapper)
  }

  @Override
  String getMockedServiceName() {
    'Email'
  }

  @Override
  void send(String templateId, String name, String email, Map<String, Object> variables) {
    execute(
      'send',
      [
        templateId: templateId,
        name: name,
        email: email,
        variables: variables,
      ],
      Void.class,
      () -> { }
    )
  }
}
