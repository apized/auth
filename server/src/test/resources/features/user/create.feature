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

#  TODO
  Scenario: Validations