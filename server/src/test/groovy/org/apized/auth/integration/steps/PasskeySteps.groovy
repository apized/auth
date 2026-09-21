package org.apized.auth.integration.steps

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.cucumber.java.BeforeAll
import io.cucumber.java.en.And
import io.restassured.response.Response
import org.apized.auth.integration.simulator.PasskeySimulator
import org.apized.test.integration.core.IntegrationConfig
import org.apized.test.integration.core.IntegrationContext
import org.apized.test.integration.core.TestRunner
import org.apized.test.integration.steps.AbstractSteps

class PasskeySteps extends AbstractSteps {

  static TestRunner testRunner
  static IntegrationContext context

  // Per-scenario simulator — same key pair used for registration and authentication
  final PasskeySimulator simulator = new PasskeySimulator()

  // Per-scenario storage of parsed challenge responses, keyed by alias
  final Map<String, Map> storedChallenges = [:]

  @BeforeAll
  static void setup() {
    testRunner = IntegrationConfig.getTestRunner()
    context = testRunner.context
  }

  @And('^I start a passkey registration challenge as ([^\\s]+)$')
  void startPasskeyRegistrationChallenge(String alias) {
    Response response = testRunner.getClient(context)
      .post(context.eval("/passkeys/challenges/register").toString())
    boolean success = (response.statusCode() / 100 as int) == 2
    context.addResponse('challenge', success, response.asString(), alias)
    if (success) {
      storedChallenges[alias] = new JsonSlurper().parseText(response.asString()) as Map
    }
  }

  @And('^I complete passkey registration using challenge ([^\\s]+) as ([^\\s]+)$')
  void completePasskeyRegistration(String challengeAlias, String passkeyAlias) {
    def challengeData = storedChallenges[challengeAlias]
    def credential = simulator.registrationResponse(
      challengeData.options.challenge as String,
      "localhost",
      "http://localhost:3000"
    )
    Response response = testRunner.getClient(context)
      .body(JsonOutput.toJson([challengeId: challengeData.challengeId, credential: credential]))
      .post(context.eval("/passkeys/register").toString())
    context.addResponse('passkey', (response.statusCode() / 100 as int) == 2, response.asString(), passkeyAlias)
  }

  @And('^I start a passkey authentication challenge as ([^\\s]+)$')
  void startPasskeyAuthenticationChallenge(String alias) {
    Response response = testRunner.getClient(context)
      .post(context.eval("/passkeys/challenges/authentication").toString())
    boolean success = (response.statusCode() / 100 as int) == 2
    context.addResponse('challenge', success, response.asString(), alias)
    if (success) {
      storedChallenges[alias] = new JsonSlurper().parseText(response.asString()) as Map
    }
  }

  @And('^I authenticate with the passkey using challenge ([^\\s]+) as user ([^\\s]+)$')
  void authenticateWithPasskey(String challengeAlias, String userAlias) {
    def challengeData = storedChallenges[challengeAlias]
    def userId = UUID.fromString(context.eval("\${${userAlias}.id}").toString())
    def userHandle = PasskeySimulator.uuidToBytes(userId)
    def credential = simulator.authenticationResponse(
      challengeData.options.challenge as String,
      "localhost",
      "http://localhost:3000",
      userHandle
    )
    Response response = testRunner.getClient(context)
      .body(JsonOutput.toJson([challengeId: challengeData.challengeId, credential: credential]))
      .post(context.eval("/passkeys/authentication").toString())
    context.addResponse('token', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @And('^I list my passkeys$')
  void listMyPasskeys() {
    Response response = testRunner.getClient(context)
      .get(context.eval("/passkeys").toString())
    context.addResponse('passkey', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @And('^the passkey page has (\\d+) elements$')
  void passkeyResponseContainsElements(int count) {
    def response = context.lastestResponse as List
    assert response.size() == count
  }

  @And('^I complete passkey registration with a non-existent challenge$')
  void completePasskeyRegistrationWithInvalidChallenge() {
    def credential = simulator.registrationResponse("fakechallenge", "localhost", "http://localhost:3000")
    Response response = testRunner.getClient(context)
      .body(JsonOutput.toJson([challengeId: UUID.randomUUID().toString(), credential: credential]))
      .post(context.eval("/passkeys/register").toString())
    context.addResponse('passkey', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @And('^I authenticate with the passkey using a non-existent challenge$')
  void authenticateWithInvalidChallenge() {
    def credential = simulator.authenticationResponse("fakechallenge", "localhost", "http://localhost:3000", new byte[16])
    Response response = testRunner.getClient(context)
      .body(JsonOutput.toJson([challengeId: UUID.randomUUID().toString(), credential: credential]))
      .post(context.eval("/passkeys/authentication").toString())
    context.addResponse('token', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @And('^I delete passkey ([^\\s]+)$')
  void deletePasskey(String alias) {
    def passkeyId = context.eval("\${${alias}.id}").toString()
    Response response = testRunner.getClient(context)
      .delete(context.eval("/passkeys/${passkeyId}").toString())
    context.addResponse('passkey', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }
}
