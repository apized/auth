Feature: Create roles

  Scenario: Anonymous can't create roles
    Given I login as anonymous
    When I create a role with
      | name | Test role |
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.role.create.name.Test role) Not allowed to create Role with `name` set to Test role |

  Scenario: Admin can create roles
    Given I login as administrator
    When I create a role with
      | name | Test role |
    Then the request succeeds
    And the response contains
      | name | Test role |

  Scenario: Validations
    Given I login as administrator
    When I create an empty role
    Then the request fails
    And the response path "errors" contains 1 elements
    And the response path "errors" contains element with
      | field   | name              |
      | message | must not be blank |
