Feature: User permission revoking

  Background:
    Given there is a user user with
      | name        | Test user       |
      | username    | user@apized.org |
      | password    | user_password   |
      | permissions | ['auth.user']   |
    And there is a user other with
      | name        | Other user                      |
      | username    | other@apized.org                |
      | password    | other_password                  |
      | permissions | ['auth.user.get', 'super.test'] |

  Scenario: User can revoke permission both users have
    Given I login as user
    When I revoke the auth.user permission from the user with id ${other.id}
    Then the request succeeds
    And I get a user with id ${other.id}
    And the response path "permissions" contains 0 elements with
      | _ | auth.user |

  Scenario: User can revoke sub-permission that he has a parent permission for
    Given I login as user
    When I revoke the auth.user.get permission from the user with id ${other.id}
    Then the request succeeds
    And I get a user with id ${other.id}
    And the response path "permissions" contains 0 elements with
      | _ | auth.user.get |

  Scenario: User can't revoke permission he doesn't have
    Given I login as user
    When I revoke the super.test permission from the user with id ${other.id}
    Then the request fails
    And the response path "errors" contains element with
      | message | (super.test) Not allowed to revoke permission super.test |

  Scenario: User can't revoke a parent permission he has a child of
    Given I login as user
    When I revoke the auth permission from the user with id ${other.id}
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth) Not allowed to revoke permission auth |

  Scenario: User can't revoke sub-permission when the target user has only the parent
    Given I login as user
    When I revoke the auth.user.get.123 permission from the user with id ${other.id}
    Then the request succeeds
    And I get a user with id ${other.id}
    And the response path "permissions" contains element with
      | _ | auth.user.get |