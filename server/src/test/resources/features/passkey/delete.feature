Feature: Delete Passkey

  Background:
    Given there is a user passkeyUser with
      | name     | Passkey User           |
      | username | passkeyUser@apized.org |
      | password | secure_password        |
      | verified | true                   |
    And I login as passkeyUser
    And I start a passkey registration challenge as regChallenge
    And I complete passkey registration using challenge regChallenge as myPasskey

  Scenario: User can delete their own passkey
    Given I login as passkeyUser
    When I delete passkey myPasskey
    Then the request succeeds
    And I list my passkeys
    And the passkey page has 0 elements

  Scenario: User cannot delete another user's passkey
    Given there is a user otherUser with
      | name     | Other User           |
      | username | otherUser@apized.org |
      | password | other_password       |
      | verified | true                 |
    And I login as otherUser
    When I delete passkey myPasskey
    Then the request fails

  Scenario: Cannot delete a passkey when not authenticated
    Given I login as anonymous
    When I delete passkey myPasskey
    Then the request fails
