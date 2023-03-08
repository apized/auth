Feature: Verify password reset

  Background:
    Given there is a user test with
      | name     | Test user       |
      | username | test@apized.org |
      | password | test_password   |
      | verified | true            |
    And I login as admin
    And I ask for the password of test@apized.org to be reset
    And the password code for test is stored as code

  Scenario: Anonymous can verify and reset password
    Given I login as anonymous
    When I reset the password for test@apized.org with code ${code} and password s3cret
    Then the request succeeds
    And I login with username test@apized.org and password s3cret
    And the request succeeds

  Scenario: Anonymous cannot verify the password reset with an invalid code
    Given I login as anonymous
    When I reset the password for test@apized.org with code 123 and password s3cret
    Then the request fails
    And I login with username test@apized.org and password s3cret
    And the request fails
    And the response path "errors" contains element with
      | message | Not authorized |

  Scenario: Anonymous cannot reset a password for a non existing user
    Given I login as anonymous
    When I reset the password for invalid@apized.org with code 123 and password s3cret
    Then the request fails
    And the response path "errors" contains element with
      | message | Not authorized |
