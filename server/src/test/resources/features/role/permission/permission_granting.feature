Feature: Role permission granting

  Background:
    Given there is a user user with
      | name        | Test user                    |
      | username    | user@apized.org              |
      | password    | user_password                |
      | permissions | [ 'auth.role', 'auth.user' ] |
    And there is a role role with
      | name | Test role |

  Scenario: User can grant permission he has to role
    Given I login as user
    When I grant the auth.user permission to the role with id ${role.id}
    Then the request succeeds
    And I get a role with id ${role.id}
    And the response path "permissions" contains element with
      | _ | auth.user |

  Scenario: User can grant sub-permission to Role
    Given I login as user
    When I grant the auth.user.get permission to the role with id ${role.id}
    Then the request succeeds
    And I get a role with id ${role.id}
    And the response path "permissions" contains element with
      | _ | auth.user.get |

  Scenario: User can't grant permission he doesn't have to role
    Given I login as user
    When I grant the auth.test permission to the role with id ${role.id}
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.test) Not allowed to grant permission auth.test |

  Scenario: User can't grant parent permission to role
    Given I login as user
    When I grant the auth permission to the role with id ${role.id}
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth) Not allowed to grant permission auth |