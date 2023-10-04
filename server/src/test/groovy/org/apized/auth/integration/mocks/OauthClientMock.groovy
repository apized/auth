package org.apized.auth.integration.mocks


import io.micronaut.context.annotation.Replaces
import io.micronaut.serde.ObjectMapper
import jakarta.inject.Singleton
import org.apized.auth.api.oauth.Oauth
import org.apized.auth.api.user.User
import org.apized.auth.oauth.AuthOauthClient
import org.apized.auth.oauth.OauthClient
import org.apized.test.integration.service.AbstractServiceIntegrationMock

@Singleton
@Replaces(AuthOauthClient.class)
class OauthClientMock extends AbstractServiceIntegrationMock implements OauthClient {
  OauthClientMock(ObjectMapper mapper) {
    super(mapper)
  }

  @Override
  User getUser(Oauth oauth, String code, String redirect) {
    addExecution('getUser', [ oauth: oauth, code: code ])
    if (getExpectation('getUser')) {
      mapper.readValue(getExpectation('getUser'), User)
    } else {
      null
    }
  }

  @Override
  String getMockedServiceName() {
    return "OauthClient"
  }
}
