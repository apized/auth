Feature: Role permission revoking

  Background:
    Given there is a user user with
      | name        | Test user                    |
      | username    | user@apized.org              |
      | password    | user_password                |
      | permissions | [ 'auth.role', 'auth.user' ] |
    And there is a role role with
      | name        | Test role                         |
      | permissions | [ 'auth.user.get', 'super.test' ] |

  Scenario: User can revoke role's permission he has
    Given I login as user
    When I revoke the auth.user permission from the role with id ${role.id}
    Then the request succeeds
    And I get a role with id ${role.id}
    And the response path "permissions" contains 0 elements with
      | _ | auth.user |

  Scenario: User can revokes role's sub-permission he has the parent permission of
    Given I login as user
    When I revoke the auth.user.get permission from the role with id ${role.id}
    Then the request succeeds
    And I get a role with id ${role.id}
    And the response path "permissions" contains 0 elements with
      | _ | auth.user.get |

  Scenario: User can't revoke role's permission he doesn't have
    Given I login as user
    When I revoke the super.test permission from the role with id ${role.id}
    Then the request fails
    And the response path "errors" contains element with
      | message | (super.test) Not allowed to revoke permission super.test |

  Scenario: User can't revoke role's parent permission he has a child of
    Given I login as user
    When I revoke the auth permission from the role with id ${role.id}
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth) Not allowed to revoke permission auth |

  Scenario: User can't revoke role's sub-permission when role has only the parent
    Given I login as user
    When I revoke the auth.user.get.123 permission from the role with id ${role.id}
    Then the request succeeds
    And I get a role with id ${role.id}
    And the response path "permissions" contains element with
      | _ | auth.user.get |
