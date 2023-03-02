Feature: Get roles

  Background:
    Given there is a role role with
      | name | test role |

  Scenario: Anonymous can't get roles
    Given I login as anonymous
    When I get a role with id ${role.id}
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.role.get.${role.id}) Not allowed to get Role with id ${role.id} |

  Scenario: Admin can get roles
    Given I login as administrator
    When I get a role with id ${role.id}
    Then the request succeeds
    And the response contains
      | name | test role |