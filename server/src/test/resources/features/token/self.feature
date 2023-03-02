Feature: Get self using token

  Background:
    Given there is a user user with
      | username | user@apized.org |
      | password | user_password   |
      | name     | Test user       |
      | verified | true            |

  Scenario: Admin gets himself on self
    Given I login as administrator
    When I get self
    Then the request succeeds
    And the response contains
      | name | Administrator |

  Scenario: User gets himself on self
    Given I login as user
    When I get self
    Then the request succeeds
    And the response contains
      | name | Test user |

  Scenario: Anonymous gets himself on self
    Given I login as anonymous
    When I get self
    Then the request succeeds
    And the response contains
      | name | Anonymous |