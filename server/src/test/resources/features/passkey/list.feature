Feature: List Passkeys

  Background:
    Given there is a user passkeyUser with
      | name     | Passkey User           |
      | username | passkeyUser@apized.org |
      | password | secure_password        |
      | verified | true                   |
    And I login as passkeyUser
    And I start a passkey registration challenge as regChallenge
    And I complete passkey registration using challenge regChallenge as myPasskey

  Scenario: User can list their registered passkeys
    Given I login as passkeyUser
    When I list my passkeys
    Then the request succeeds
    And the passkey page has 1 elements

  Scenario: User only sees their own passkeys
    Given there is a user otherUser with
      | name     | Other User           |
      | username | otherUser@apized.org |
      | password | other_password       |
      | verified | true                 |
    And I login as otherUser
    When I list my passkeys
    Then the request succeeds
    And the passkey page has 0 elements