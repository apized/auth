Feature: Passkey Authentication

  Scenario: User with a registered passkey can authenticate
    Given there is a user passkeyUser with
      | name     | Passkey User           |
      | username | passkeyUser@apized.org |
      | password | secure_password        |
      | verified | true                   |
    And I login as passkeyUser
    And I start a passkey registration challenge as regChallenge
    And I complete passkey registration using challenge regChallenge as myPasskey
    And I login as anonymous
    When I start a passkey authentication challenge as authChallenge
    And I authenticate with the passkey using challenge authChallenge as user passkeyUser
    Then the request succeeds
    And the response contains
      | jwt | /.*/ |

  Scenario: Authentication fails with a non-existent challenge
    Given I login as anonymous
    When I authenticate with the passkey using a non-existent challenge
    Then the request fails

  Scenario: Cannot replay an authentication challenge
    Given there is a user passkeyUser with
      | name     | Passkey User           |
      | username | passkeyUser@apized.org |
      | password | secure_password        |
      | verified | true                   |
    And I login as passkeyUser
    And I start a passkey registration challenge as regChallenge
    And I complete passkey registration using challenge regChallenge as myPasskey
    And I login as anonymous
    And I start a passkey authentication challenge as authChallenge
    And I authenticate with the passkey using challenge authChallenge as user passkeyUser
    When I authenticate with the passkey using challenge authChallenge as user passkeyUser
    Then the request fails

  Scenario: Unverified user cannot authenticate with a passkey
    Given there is a user unverifiedUser with
      | name     | Unverified User           |
      | username | unverifiedUser@apized.org |
      | password | some_password             |
    And I login as unverifiedUser
    And I start a passkey registration challenge as regChallenge
    And I complete passkey registration using challenge regChallenge as myPasskey
    And I login as anonymous
    When I start a passkey authentication challenge as authChallenge
    And I authenticate with the passkey using challenge authChallenge as user unverifiedUser
    Then the request fails
    And the response path "errors" contains element with
      | message | Email verification pending |
