Feature: Update roles

  Background:
    Given there is a role role with
      | name | test role |

  Scenario: Anonymous can't update roles
    Given I login as anonymous
    When I update a role with id ${role.id} with
      | name | updated role |
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.role.get.${role.id}) Not allowed to get Role with id ${role.id} |

  Scenario: Admin can update roles
    Given I login as administrator
    When I update a role with id ${role.id} with
      | name | updated role |
    Then the request succeeds
    And the response contains
      | name | updated role |

#  TODO
  Scenario: Validations