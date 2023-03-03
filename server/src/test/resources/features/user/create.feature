Feature: Create users

  Scenario: Anonymous can create users
    Given I login as anonymous
    When I create a user with
      | name     | Test user       |
      | username | user@apized.org |
      | password | user_password   |
    Then the request succeeds
    And the response contains
      | name     | Test user       |
      | username | user@apized.org |
      | verified | false           |

  Scenario: Admin can create users
    Given I login as administrator
    When I create a user with
      | name     | Test user       |
      | username | user@apized.org |
      | password | user_password   |
    Then the request succeeds
    And the response contains
      | name     | Test user       |
      | username | user@apized.org |
      | verified | false           |

  Scenario: Anonymous can only create unverified users
    Given I login as anonymous
    When I create a user with
      | name     | Test user       |
      | username | user@apized.org |
      | password | user_password   |
      | verified | true            |
    Then the request succeeds
    And the response contains
      | name     | Test user       |
      | username | user@apized.org |
      | verified | false           |

  Scenario: Admin can create already verified users
    Given I login as administrator
    When I create a user with
      | name     | Test user       |
      | username | user@apized.org |
      | password | user_password   |
      | verified | true            |
    Then the request succeeds
    And the response contains
      | name     | Test user       |
      | username | user@apized.org |
      | verified | true            |

  Scenario: Validations
    Given I login as administrator
    When I create an empty user
    Then the request fails
    And the response path "errors" contains 2 elements
    And the response path "errors" contains element with
      | field   | username         |
      | message | must not be null |
    And the response path "errors" contains element with
      | field   | name             |
      | message | must not be null |
