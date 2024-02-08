package org.apized.auth.integration.mocks

import io.micronaut.context.annotation.Replaces
import io.micronaut.serde.ObjectMapper
import jakarta.inject.Singleton
import org.apized.core.event.ESBAdapter
import org.apized.micronaut.messaging.rabbitmq.RabbitMQESBAdapter
import org.apized.test.integration.service.AbstractServiceIntegrationMock

@Singleton
@Replaces(RabbitMQESBAdapter.class)
class ESBAdapterMock extends AbstractServiceIntegrationMock implements ESBAdapter {
  @Override
  String getMockedServiceName() {
    'ESB'
  }

  ESBAdapterMock(ObjectMapper mapper) {
    super(mapper)
  }

  @Override
  void send(UUID messageId, Date timestamp, String topic, Map<String, Object> headers, Object payload) {
    execute(
      'send',
      [
        messageId: messageId,
        timestamp: timestamp,
        topic: topic,
        headers: headers,
        payload: payload
      ],
      Void,
      () -> null
    )
  }
}
