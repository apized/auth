package org.apized.auth.passkey;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import lombok.Data;

import java.util.Arrays;
import java.util.stream.Collectors;

@Data
@Singleton
public class PasskeyConfig {

  final RelyingParty relyingParty;

  PasskeyConfig(
    PasskeyCredentialService credentialRepository,
    @Value("${auth.passkey.rp-id}") String rpId,
    @Value("${auth.passkey.rp-name}") String rpName,
    @Value("${auth.passkey.origins}") String origins
  ) {
    relyingParty = RelyingParty.builder()
      .identity(RelyingPartyIdentity.builder().id(rpId).name(rpName).build())
      .credentialRepository(credentialRepository)
      .origins(Arrays.stream(origins.split(",")).map(String::trim).collect(Collectors.toSet()))
      .build();
  }
}
