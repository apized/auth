Feature: Delete roles

  Background:
    Given there is a user user with
      | username | user@apized.org |
      | password | user_password   |
      | name     | Test user       |
      | verified | true            |
    Given there is a role role with
      | name | test role |
    And there is a role empty with
      | name | empty role |
    And I login as administrator
    And I update a user with id ${user.id} with
      | roles | [ '${user.roles[0]}', '${role.id}' ] |

  Scenario: Admin can delete empty roles
    Given I login as administrator
    When I delete an role with id ${empty.id}
    Then the request succeeds

  Scenario: Admin can delete non-empty roles (without removing users)
    Given I login as administrator
    When I delete an role with id ${role.id}
    Then the request succeeds
    And I list the users
    And the response contains
      | total | 2 |

  Scenario: Anonymous can't delete empty roles
    Given I login as anonymous
    When I delete an role with id ${empty.id}
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.role.delete.${empty.id}) Not allowed to delete Role with id ${empty.id} |

  Scenario: Anonymous can't delete non-empty roles
    Given I login as anonymous
    When I delete an role with id ${role.id}
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.role.delete.${role.id}) Not allowed to delete Role with id ${role.id} |
