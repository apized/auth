package org.apized.auth.integration.simulator

import com.upokecenter.cbor.CBORObject
import groovy.json.JsonOutput

import java.nio.ByteBuffer
import java.security.*
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

/**
 * Simulates a software WebAuthn authenticator for integration testing.
 * Holds a single key pair and credential ID across registration and authentication.
 */
class PasskeySimulator {

  final byte[] credentialId
  final KeyPair keyPair

  PasskeySimulator() {
    def gen = KeyPairGenerator.getInstance("EC")
    gen.initialize(new ECGenParameterSpec("secp256r1"))
    keyPair = gen.generateKeyPair()
    credentialId = new byte[32]
    new SecureRandom().nextBytes(credentialId)
  }

  Map<String, Object> registrationResponse(String challengeBase64Url, String rpId, String origin) {
    def clientDataJSON = JsonOutput.toJson([
      type       : "webauthn.create",
      challenge  : challengeBase64Url,
      origin     : origin,
      crossOrigin: false
    ])
    def rpIdHash = sha256(rpId.bytes)
    def coseKey = encodeCoseKey((ECPublicKey) keyPair.public)
    def authData = buildRegistrationAuthData(rpIdHash, coseKey)
    def attestationObject = buildAttestationObject(authData)

    return [
      id                    : base64UrlEncode(credentialId),
      rawId                 : base64UrlEncode(credentialId),
      type                  : "public-key",
      response              : [
        clientDataJSON   : base64UrlEncode(clientDataJSON.bytes),
        attestationObject: base64UrlEncode(attestationObject)
      ],
      clientExtensionResults: [appid: false]
    ]
  }

  Map<String, Object> authenticationResponse(String challengeBase64Url, String rpId, String origin, byte[] userHandle) {
    def clientDataJSON = JsonOutput.toJson([
      type       : "webauthn.get",
      challenge  : challengeBase64Url,
      origin     : origin,
      crossOrigin: false
    ])
    def rpIdHash = sha256(rpId.bytes)
    def authData = buildAuthenticationAuthData(rpIdHash)
    def signature = sign(authData, clientDataJSON.bytes)

    return [
      id                    : base64UrlEncode(credentialId),
      rawId                 : base64UrlEncode(credentialId),
      type                  : "public-key",
      response              : [
        clientDataJSON   : base64UrlEncode(clientDataJSON.bytes),
        authenticatorData: base64UrlEncode(authData),
        signature        : base64UrlEncode(signature),
        userHandle       : base64UrlEncode(userHandle)
      ],
      clientExtensionResults: [appid: false]
    ]
  }

  private byte[] buildRegistrationAuthData(byte[] rpIdHash, byte[] coseKey) {
    def out = new ByteArrayOutputStream()
    out.write(rpIdHash)
    out.write(0x45)           // flags: UP | UV | AT (attested credential data present)
    out.write(new byte[4])    // signCount = 0
    out.write(new byte[16])   // AAGUID = zeros (no AAGUID)
    out.write(ByteBuffer.allocate(2).putShort((short) credentialId.length).array())
    out.write(credentialId)
    out.write(coseKey)
    out.toByteArray()
  }

  private byte[] buildAuthenticationAuthData(byte[] rpIdHash) {
    def out = new ByteArrayOutputStream()
    out.write(rpIdHash)
    out.write(0x05)           // flags: UP | UV
    out.write(new byte[4])    // signCount = 0
    out.toByteArray()
  }

  private byte[] buildAttestationObject(byte[] authData) {
    def obj = CBORObject.NewMap()
    obj.Add("fmt", CBORObject.FromObject("none"))
    obj.Add("attStmt", CBORObject.NewMap())
    obj.Add("authData", CBORObject.FromObject(authData))
    obj.EncodeToBytes()
  }

  private byte[] encodeCoseKey(ECPublicKey key) {
    def x = toBytes32(key.W.affineX)
    def y = toBytes32(key.W.affineY)

    def coseKey = CBORObject.NewMap()
    coseKey.Add(CBORObject.FromObject(1 as int), CBORObject.FromObject(2 as int))   // kty: EC2
    coseKey.Add(CBORObject.FromObject(3 as int), CBORObject.FromObject(-7 as int))  // alg: ES256
    coseKey.Add(CBORObject.FromObject(-1 as int), CBORObject.FromObject(1 as int))  // crv: P-256
    coseKey.Add(CBORObject.FromObject(-2 as int), CBORObject.FromObject(x))         // x
    coseKey.Add(CBORObject.FromObject(-3 as int), CBORObject.FromObject(y))         // y
    coseKey.EncodeToBytes()
  }

  private byte[] sign(byte[] authData, byte[] clientDataBytes) {
    def sig = Signature.getInstance("SHA256withECDSA")
    sig.initSign(keyPair.private)
    sig.update(authData)
    sig.update(sha256(clientDataBytes))
    sig.sign()
  }

  private static byte[] sha256(byte[] data) {
    MessageDigest.getInstance("SHA-256").digest(data)
  }

  private static byte[] toBytes32(BigInteger n) {
    def bytes = n.toByteArray()
    if (bytes.length == 32) return bytes
    if (bytes.length > 32) return bytes[(bytes.length - 32)..<bytes.length] as byte[]
    def result = new byte[32]
    System.arraycopy(bytes, 0, result, 32 - bytes.length, bytes.length)
    result
  }

  static String base64UrlEncode(byte[] bytes) {
    Base64.urlEncoder.withoutPadding().encodeToString(bytes)
  }

  static byte[] uuidToBytes(UUID uuid) {
    ByteBuffer.allocate(16).tap {
      putLong(uuid.mostSignificantBits)
      putLong(uuid.leastSignificantBits)
    }.array()
  }
}
