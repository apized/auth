Feature: Passkey Registration

  Background:
    Given there is a user passkeyUser with
      | name     | Passkey User           |
      | username | passkeyUser@apized.org |
      | password | secure_password        |
      | verified | true                   |

  Scenario: Authenticated user can start a registration challenge
    Given I login as passkeyUser
    When I start a passkey registration challenge as regChallenge
    Then the request succeeds
    And the response contains
      | challengeId | /.*/ |

  Scenario: Authenticated user can complete registration
    Given I login as passkeyUser
    When I start a passkey registration challenge as regChallenge
    And I complete passkey registration using challenge regChallenge as myPasskey
    Then the request succeeds
    And the response contains
      | credentialId | /.*/ |

  Scenario: Cannot start a registration challenge when not authenticated
    Given I login as anonymous
    When I start a passkey registration challenge as regChallenge
    Then the request fails

  Scenario: Cannot complete registration with a non-existent challenge
    Given I login as passkeyUser
    When I complete passkey registration with a non-existent challenge
    Then the request fails

  Scenario: Cannot replay a completed registration challenge
    Given I login as passkeyUser
    And I start a passkey registration challenge as regChallenge
    And I complete passkey registration using challenge regChallenge as myPasskey
    When I complete passkey registration using challenge regChallenge as replayedPasskey
    Then the request fails
