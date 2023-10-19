package org.apized.auth.api.oauth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apized.auth.oauth.OauthProvider;
import org.apized.core.model.Apized;
import org.apized.core.model.BaseModel;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Getter
@Setter
@Entity
@Serdeable
@NoArgsConstructor
@Apized(extensions = {OauthRepositoryExtension.class})
public class Oauth extends BaseModel {
  @NotNull
  @Size(min = 3)
  private String name;

  @NotNull
  @Size(min = 3, max = 15)
  @Pattern(regexp = "[a-z0-9-]+")
  private String slug;

  @NotNull
  @Enumerated(EnumType.STRING)
  private OauthProvider provider;

  @NotNull
  private String clientId;

  @NotNull
  private String clientSecret;

  @JsonIgnore
  @Transient
  @SneakyThrows
  public String getComputedClientSecret() {
    Optional<OauthProvider> providerOptional = Optional.ofNullable(getProvider());
    if (providerOptional.isPresent() && providerOptional.get().equals(OauthProvider.Apple)) {
      String privateKey = getClientSecret()
        .replaceAll("-----(BEGIN|END)(.*?)-----", "")
        .replaceAll("\\s+", "");

      Date now = new Date();
      return JWT.create()
        .withIssuer("XMSCUH3K2A")
        .withIssuedAt(now)
        .withExpiresAt(now.toInstant().plus(5, ChronoUnit.MINUTES))
        .withAudience("https://appleid.apple.com")
        .withSubject(getClientId())
        .sign(Algorithm.ECDSA256(
          (ECPrivateKey) KeyFactory.getInstance("EC")
            .generatePrivate(
              new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey))
            )
        ));
    } else {
      return getClientSecret();
    }
  }

  @Transient
  public String getLoginUrl() {
    Optional<OauthProvider> providerOptional = Optional.ofNullable(getProvider());
    return providerOptional.isPresent() ? switch (getProvider()) {
      case Google -> String.format(
        "https://accounts.google.com/o/oauth2/v2/auth?response_type=code&scope=email%%20profile&client_id=%s",
        getClientId()
      );
      case Apple -> String.format(
        "https://appleid.apple.com/auth/authorize?client_id=%s&response_type=code&response_mode=form_post&scope=name%%20email",
        getClientId()
      );
      case Microsoft -> String.format(
        "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?response_type=code&client_id=%s&scope=openid%%20email%%20profile",
        getClientId()
      );
      case Facebook -> String.format(
        "https://www.facebook.com/dialog/oauth?response_type=code&client_id=%s&scope=email",
        getClientId()
      );
      case GitHub -> String.format(
        "https://github.com/login/oauth/authorize?client_id=%s&response_type=code",
        getClientId()
      );
      case Slack -> String.format(
        "https://slack.com/oauth/authorize?client_id=%s&scope=identity.basic,identity.email",
        getClientId()
      );
      case LinkedIn -> String.format(
        "https://www.linkedin.com/oauth/v2/authorization?client_id=%s&response_type=code&scope=r_liteprofile%%20r_emailaddress",
        getClientId()
      );
    } : null;
  }

  @JsonIgnore
  @Transient
  public String getAccessTokenUrl() {
    Optional<OauthProvider> providerOptional = Optional.ofNullable(getProvider());
    return providerOptional.isPresent() ? switch (getProvider()) {
      case Google ->
        "https://www.googleapis.com/oauth2/v4/token?client_id={client_id}&client_secret={client_secret}&grant_type=authorization_code";
      case Apple ->
        "https://appleid.apple.com/auth/token?client_id={client_id}&client_secret={client_secret}&grant_type=authorization_code";
      case Microsoft ->
        "https://login.microsoftonline.com/common/oauth2/v2.0/token?client_id={client_id}&client_secret={client_secret}&grant_type=authorization_code";
      case Facebook ->
        "https://graph.facebook.com/v2.8/oauth/access_token?client_id={client_id}&client_secret={client_secret}";
      case GitHub -> "https://github.com/login/oauth/access_token?client_id={client_id}&client_secret={client_secret}";
      case Slack -> "https://slack.com/api/oauth.access?client_id={client_id}&client_secret={client_secret}";
      case LinkedIn ->
        "https://api.linkedin.com/oauth/v2/accessToken?client_id={client_id}&client_secret={client_secret}&grant_type=authorization_code";
    } : null;
  }

  public String getAccessTokenFrom(Map<String, Object> accessTokenResponse) {
    Optional<OauthProvider> providerOptional = Optional.ofNullable(getProvider());
    if (providerOptional.isPresent() && providerOptional.get().equals(OauthProvider.Apple)) {
      accessTokenResponse.put(
        "email", JWT.decode((String) accessTokenResponse.get("id_token")).getClaim("email").asString()
      );
    } else if (providerOptional.isPresent() && providerOptional.get().equals(OauthProvider.Microsoft)) {
      DecodedJWT jwt = JWT.decode((String) accessTokenResponse.get("id_token"));
      accessTokenResponse.put("name", jwt.getClaim("name").asString());
      accessTokenResponse.put("email", jwt.getClaim("email").asString());
    }
    return (String) accessTokenResponse.get("access_token");
  }

  @JsonIgnore
  @Transient
  public String getUserUrl() {
    Optional<OauthProvider> providerOptional = Optional.ofNullable(getProvider());
    return providerOptional.isPresent() ? switch (getProvider()) {
      case Google -> "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token={token}";
      case Facebook -> "https://graph.facebook.com/v2.8/me?fields=name,email&access_token={token}";
      case GitHub -> "https://api.github.com/user?access_token={token}";
      case LinkedIn ->
        "https://api.linkedin.com/v2/me?projection=(localizedFirstName,localizedLastName)&oauth2_access_token={token}";
      default -> null;
    } : null;
  }

  @JsonIgnore
  @Transient
  public Map<String, String> getUserHeaders() {
    if (getProvider() != null && Objects.requireNonNull(getProvider()) == OauthProvider.GitHub) {
      return Map.of("Authorization", "token {token}");
    }
    return null;
  }

  @JsonIgnore
  @Transient
  public String getEmailUrl() {
    if (getProvider() != null && Objects.requireNonNull(getProvider()) == OauthProvider.LinkedIn) {
      return "https://api.linkedin.com/v2/clientAwareMemberHandles?q=members&projection=(elements*(primary,type,handle~))&oauth2_access_token={token}";
    }
    return null;
  }

  @JsonIgnore
  @Transient
  public Map<String, String> getEmailHeaders() {
    //Not needed for any current providers
    return null;
  }

  @JsonIgnore
  @Transient
  public Map<String, String> getMapping() {
    Optional<OauthProvider> providerOptional = Optional.ofNullable(getProvider());
    return providerOptional.isPresent() ? switch (getProvider()) {
      case Apple -> Map.of("name", "name.firstName name.lastName");
      case Slack -> Map.of("name", "user.name", "email", "user.email");
      case LinkedIn ->
        Map.of("name", "localizedFirstName localizedLastName", "email", "elements.0.handle~.emailAddress");
      default -> null;
    } : null;
  }
}
