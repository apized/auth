Feature: Verify email

  Background:
    Given there is a user test with
      | name     | Test user       |
      | username | test@apized.org |
      | password | test_password   |
    And the verification code for test is stored as code

  Scenario: Admin can verify users email
    Given I login as admin
    When I verify user test@apized.org with code ${code}
    Then the request succeeds

  Scenario: Admin cannot verify users email with an invalid code
    Given I login as admin
    When I verify user test@apized.org with code 123
    Then the request fails
    And the response path "errors" contains element with
      | message | Not authorized |

  Scenario: Admin cannot verify email for non existing user
    Given I login as admin
    When I verify user idontexist with code 123
    Then the request fails
    And the response path "errors" contains element with
      | message | Not authorized |

  Scenario: Anonymous can verify email
    Given I login as anonymous
    When I verify user test@apized.org with code ${code}
    Then the request succeeds

  Scenario: Anonymous cannot verify their email with an invalid code
    Given I login as anonymous
    When I verify user test@apized.org with code 123
    Then the request fails
    And the response path "errors" contains element with
      | message | Not authorized |

  Scenario: Anonymous cannot verify email for non existing user
    Given I login as anonymous
    When I verify user idontexist with code 123
    Then the request fails
    And the response path "errors" contains element with
      | message | Not authorized |
