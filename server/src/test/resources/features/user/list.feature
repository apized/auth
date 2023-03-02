Feature: List users

  Background:
    Given there is a user test with
      | name        | Test user       |
      | username    | test@apized.org |
      | password    | test_password   |

  Scenario: Anonymous can't list users
    Given I login as anonymous
    When I list the users
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.user.list) Not allowed to list User |

  Scenario: Admin can list users
    Given I login as administrator
    When I list the users
    Then the request succeeds
    And the response path "content" contains 2 elements
