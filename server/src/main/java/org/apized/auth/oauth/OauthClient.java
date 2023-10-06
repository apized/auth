package org.apized.auth.oauth;

import org.apized.auth.api.oauth.Oauth;
import org.apized.auth.api.user.User;

import java.util.Map;

public interface OauthClient {
  User getUser(Oauth oauth, String code, Map<String,Object> props, String redirect);
}
