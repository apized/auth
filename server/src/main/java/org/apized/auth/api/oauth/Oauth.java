package org.apized.auth.api.oauth;

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
import org.apized.auth.oauth.OauthProvider;
import org.apized.core.model.Apized;
import org.apized.core.model.BaseModel;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

  @Transient
  public String getLoginUrl() {
    Optional<OauthProvider> providerOptional = Optional.ofNullable(getProvider());
    return providerOptional.isPresent() ? switch (getProvider()) {
      case Google -> String.format(
        "https://accounts.google.com/o/oauth2/v2/auth?response_type=code&scope=email%%20profile&client_id=%s",
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
      case Facebook ->
        "https://graph.facebook.com/v2.8/oauth/access_token?client_id={client_id}&client_secret={client_secret}";
      case GitHub -> "https://github.com/login/oauth/access_token?client_id={client_id}&client_secret={client_secret}";
      case Slack -> "https://slack.com/api/oauth.access?client_id={client_id}&client_secret={client_secret}";
      case LinkedIn ->
        "https://api.linkedin.com/oauth/v2/accessToken?client_id={client_id}&client_secret={client_secret}&grant_type=authorization_code";
    } : null;
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
      case Slack -> Map.of("name", "user.name", "email", "user.email");
      case LinkedIn ->
        Map.of("name", "localizedFirstName localizedLastName", "email", "elements.0.handle~.emailAddress");
      default -> null;
    } : null;
  }
}