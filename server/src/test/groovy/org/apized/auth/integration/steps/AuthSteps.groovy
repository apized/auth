package org.apized.auth.integration.steps

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import groovy.json.JsonOutput
import io.cucumber.datatable.DataTable
import io.cucumber.java.BeforeAll
import io.cucumber.java.en.And
import io.cucumber.java.en.When
import io.restassured.response.Response
import org.apized.test.integration.core.IntegrationConfig
import org.apized.test.integration.core.IntegrationContext
import org.apized.test.integration.core.TestRunner
import org.apized.test.integration.steps.AbstractSteps

import java.time.LocalDateTime
import java.time.ZoneId

import static org.junit.jupiter.api.Assertions.assertTrue

class AuthSteps extends AbstractSteps {
  static TestRunner testRunner
  static IntegrationContext context

  @BeforeAll
  static void setup() {
    testRunner = IntegrationConfig.getTestRunner()
    context = testRunner.context
  }

  private void executeAs(String user, Runnable closure) {
    String oldToken = testRunner.getToken()
    testRunner.setUserAs(user)
    closure.run()
    testRunner.setToken(oldToken)
  }

  @And('^there is a user ([^\\s]+) with$')
  void createUser(String alias, DataTable table) {
    executeAs('administrator', () -> {
      Map<String, Object> userInput = table ? new HashMap<>(table.asMap(String, String)) : new HashMap<>()
      userInput.password = userInput.password ?: UUID.randomUUID().toString()
      userInput.remove("permissions")

      Response response = testRunner.getClient()
        .body(JsonOutput.toJson(userInput))
        .post(context.eval("/users") as String)
      context.addResponse('user', (response.statusCode() / 100 as int) == 2, response.asString(), alias)

      String userId = context.eval("\${${alias}.id}").toString()
      for (String permission : context.eval((table?.asMap(String, Object)?.permissions ?: '[]') as String)) {
        grantPermission(permission, 'user', userId)
        assertTrue(context.getLatestStatus())
      }
    })
  }

  @And('^there is a role ([^\\s]+) with$')
  void createRole(String alias, DataTable table) {
    executeAs('administrator', () -> {
      Response response = testRunner.getClient()
        .body(JsonOutput.toJson(table.asMap(String, String)))
        .post(context.eval("/roles") as String)
      context.addResponse('role', (response.statusCode() / 100 as int) == 2, response.asString(), alias)

      String roleId = context.eval("\${${alias}.id}").toString()
      for (String permission : context.eval((table?.asMap(String, Object)?.permissions ?: '[]') as String)) {
        grantPermission(permission, 'role', roleId)
        assertTrue(context.getLatestStatus())
      }
    })
  }

  @And('^I grant the ([^\\s]+) permission to the (user|role) with id ([^\\s]+)$')
  void grantPermission(String permission, String type, String userId) {
    Response response = testRunner.getClient(context)
      .post(context.eval("/${type}s/$userId/permissions/$permission").toString())
    context.addResponse('user', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @And('^I revoke the ([^\\s]+) permission from the (user|role) with id ([^\\s]+)$')
  void revokePermission(String permission, String type, String userId) {
    Response response = testRunner.getClient(context)
      .delete(context.eval("/${type}s/$userId/permissions/$permission").toString())
    context.addResponse('user', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @And('^I login with username ([^\\s]+) and password ([^\\s]+)$')
  void login(String username, String password) {
    Response response = testRunner.getClient(context)
      .body(JsonOutput.toJson([ username: username, password: password ]))
      .post(context.eval("/tokens").toString())
    context.addResponse('user', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @And('^There is a token for user ([^\\s]+) as ([^\\s]+) valid for (\\d+) seconds$')
  void tokenFor(String user, String alias, Integer duration) {
    Algorithm algorithm = Algorithm.HMAC256('boatymcboatface')
    String userId = testRunner.getUserId(user)
    Date issuedAt = new Date()

    JWTCreator.Builder builder = JWT.create()
      .withIssuer('apized')
      .withAudience('apized')
      .withJWTId(UUID.randomUUID().toString())
      .withIssuedAt(issuedAt)
      .withSubject(userId)

    LocalDateTime expiry = issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().plusSeconds(duration)
    builder.withExpiresAt(Date.from(expiry.atZone(ZoneId.systemDefault()).toInstant()))

    context.addResponse('token', true, "{\"jwt\": \"${builder.sign(algorithm)}\"}", alias)
  }

  @And('^I redeem the token ([^\\s]+)$')
  void redeemToken(String token) {
    Response response = testRunner.getClient(context)
      .body()
      .get(context.eval("/tokens/$token").toString())
    context.addResponse('token', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @And('^I renew the token ([^\\s]+)$')
  void iRenewTheToken(String token) {
    Response response = testRunner.getClient(context)
      .body()
      .put(context.eval("/tokens/$token").toString())
    context.addResponse('token', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @And('I get self')
  void getSelf() {
    Response response = testRunner.getClient(context)
      .body()
      .get(context.eval("/tokens").toString())
    context.addResponse('token', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @When('^I create an (expiring|non-expiring) token for ([^\\s]+)$')
  void createTokenFor(String type, String user) {
    String userId = testRunner.getUserId(user)
    Response response = testRunner.getClient(context)
      .body()
      .post(context.eval("/tokens/$userId?expiring=${type == 'expiring'}").toString())
    context.addResponse('token', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @And('^the verification code for ([^\\s]+) is stored as ([^\\s]+)$')
  void retrieveVerificationCode(String user, String alias) {
    executeAs('administrator', () -> {
      String userId = testRunner.getUserId(user)
      Response response = testRunner.getClient(context)
        .body()
        .get(context.eval("/integration/users/$userId/emailVerificationCode").toString())
      context.responses[ alias ] = response.asString()
    })
  }

  @And('^the password code for ([^\\s]+) is stored as ([^\\s]+)$')
  void retrievePasswordCode(String user, String alias) {
    executeAs('administrator', () -> {
      String userId = testRunner.getUserId(user)
      Response response = testRunner.getClient(context)
        .body()
        .get(context.eval("/integration/users/$userId/passwordResetCode").toString())
      context.responses[ alias ] = response.asString()
    })
  }

  @When('^I verify user ([^\\s]+) with code ([^\\s]+)$')
  void verifyUser(String user, String code) {
    String userId = testRunner.getUserId(user)
    Response response = testRunner.getClient(context)
      .body(JsonOutput.toJson([ code: context.eval(code) ]))
      .post(context.eval("/users/$userId/verification").toString())
    context.addResponse('code', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @When('^I ask for the password of ([^\\s]+) to be reset$')
  void requestResetPassword(String username) {
    Response response = testRunner.getClient(context)
      .delete(context.eval("/users/$username/password").toString())
    context.addResponse('code', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @When('^I reset the password for ([^\\s]+) with code ([^\\s]+) and password ([^\\s]+)$')
  void resetPassword(String username, String code, String password) {
    Response response = testRunner.getClient(context)
      .body(JsonOutput.toJson([ code: context.eval(code), password: password ]))
      .post(context.eval("/users/$username/password").toString())
    context.addResponse('code', (response.statusCode() / 100 as int) == 2, response.asString(), null)
  }

  @When('^there is an oauth named "(.+)" as ([^\\s]+)$')
  void createAliasedOauth(String name, String alias) {
    Map<String, String> userInput = [
      name: 'TestAuth',
      slug: 'oauth-1',
      provider: 'Google',
      clientId: 'client',
      clientSecret: 'secret',
    ]

//    if (table) {
//      userInput.putAll(table.asMap(String, String))
//    }

    userInput.name = name
    executeAs('administrator', () -> {
      Response response = testRunner.getClient()
        .body(JsonOutput.toJson(userInput))
        .post(context.eval("/oauths") as String)
      context.addResponse('oauth', (response.statusCode() / 100 as int) == 2, response.asString(), alias)
    })
  }
}
