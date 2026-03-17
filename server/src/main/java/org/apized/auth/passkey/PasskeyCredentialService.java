package org.apized.auth.passkey;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apized.auth.api.passkey.Passkey;
import org.apized.auth.api.passkey.PasskeyRepository;
import org.apized.auth.api.user.UserRepository;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class PasskeyCredentialService implements CredentialRepository {

  @Inject
  PasskeyRepository passkeyRepository;

  @Inject
  UserRepository userRepository;

  @Override
  public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
    return userRepository.findByUsername(username)
      .map(user -> passkeyRepository.findByUserId(user.getId()).stream()
        .map(p -> PublicKeyCredentialDescriptor.builder()
          .id(byteArrayFromBase64Url(p.getCredentialId()))
          .transports(parseTransports(p.getTransports()))
          .build())
        .collect(Collectors.toSet()))
      .orElseGet(Set::of);
  }

  @Override
  public Optional<ByteArray> getUserHandleForUsername(String username) {
    return userRepository.findByUsername(username)
      .map(user -> uuidToByteArray(user.getId()));
  }

  @Override
  public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
    UUID userId = byteArrayToUuid(userHandle);
    return userRepository.get(userId).map(user -> user.getUsername());
  }

  @Override
  public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
    return passkeyRepository.findByCredentialId(credentialId.getBase64Url())
      .map(p -> toRegisteredCredential(p));
  }

  @Override
  public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
    return passkeyRepository.findByCredentialId(credentialId.getBase64Url())
      .map(p -> Set.of(toRegisteredCredential(p)))
      .orElseGet(Set::of);
  }

  public static ByteArray uuidToByteArray(UUID uuid) {
    ByteBuffer buf = ByteBuffer.allocate(16);
    buf.putLong(uuid.getMostSignificantBits());
    buf.putLong(uuid.getLeastSignificantBits());
    return new ByteArray(buf.array());
  }

  public static UUID byteArrayToUuid(ByteArray bytes) {
    ByteBuffer buf = ByteBuffer.wrap(bytes.getBytes());
    return new UUID(buf.getLong(), buf.getLong());
  }

  private static ByteArray byteArrayFromBase64Url(String base64Url) {
    try {
      return ByteArray.fromBase64Url(base64Url);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Set<AuthenticatorTransport> parseTransports(String transports) {
    if (transports == null || transports.isBlank()) return Set.of();
    return java.util.Arrays.stream(transports.split(","))
      .map(AuthenticatorTransport::of)
      .collect(Collectors.toSet());
  }

  private RegisteredCredential toRegisteredCredential(Passkey p) {
    return RegisteredCredential.builder()
      .credentialId(byteArrayFromBase64Url(p.getCredentialId()))
      .userHandle(uuidToByteArray(p.getUser().getId()))
      .publicKeyCose(new ByteArray(p.getPublicKeyCose()))
      .signatureCount(p.getSignCount())
      .build();
  }
}
