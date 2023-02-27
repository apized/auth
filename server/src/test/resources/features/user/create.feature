Feature: Create users

  Scenario: Anonymous can create users
    Given I login as anonymous
    When I list the users
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.user.list) Not allowed to list User |

  Scenario: Admin can create users
    Given I login as admin
    When I list the users
    Then the request succeeds

  Scenario: TODO: Validations