Feature: Password reset

  Background:
    Given there is a user verified with
      | name     | Verified user       |
      | username | verified@apized.org |
      | password | verified_password   |
      | verified | true                |
    Given there is a user unverified with
      | name     | Unverified user       |
      | username | unverified@apized.org |
      | password | unverified_password   |

  Scenario: Anonymous can request to reset a verified user password
    Given I login as anonymous
    When I ask for the password of verified@apized.org to be reset
    Then the request succeeds

  Scenario: Anonymous can request to reset a non verified user password
    Given I login as anonymous
    When I ask for the password of unverified@apized.org to be reset
    Then the request succeeds

  Scenario: Anonymous can request to reset a non existing user password
    Given I login as anonymous
    When I ask for the password of invalid@apized.org to be reset
    Then the request succeeds
