package org.apized.auth.oauth;

import org.apized.auth.api.oauth.Oauth;
import org.apized.auth.api.user.User;

public interface OauthClient {
  User getUser(Oauth oauth, String code, String redirect);
}
