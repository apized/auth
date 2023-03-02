Feature: List roles

  Background:
    Given there is a role role with
      | name | test role |

  Scenario: Anonymous can't list roles
    Given I login as anonymous
    When I list the roles
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.role.list) Not allowed to list Role |

  Scenario: Admin can list roles
    Given I login as administrator
    When I list the roles
    Then the request succeeds
    And the response path "content" contains 2 elements