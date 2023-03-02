Feature: User permission granting

  Background:
    Given there is a user user with
      | name        | Test user       |
      | username    | user@apized.org |
      | password    | user_password   |
      | permissions | ['auth.user']   |
    And there is a user other with
      | name     | Other user       |
      | username | other@apized.org |
      | password | other_password   |

  Scenario: Can grant permission to other users
    Given I login as user
    When I grant the auth.user permission to the user with id ${other.id}
    Then the request succeeds
    And I get a user with id ${other.id}
    And the response path "permissions" contains element with
      | _ | auth.user |

  Scenario: Can grant sub-permission to other users
    Given I login as user
    When I grant the auth.user.get permission to the user with id ${other.id}
    Then the request succeeds
    And I get a user with id ${other.id}
    And the response path "permissions" contains element with
      | _ | auth.user.get |

  Scenario: User can't grant permission he doesn't have to other users
    Given I login as user
    When I grant the auth.role permission to the user with id ${other.id}
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.role) Not allowed to grant permission auth.role |

  Scenario: User can't grant parent permission to other users
    Given I login as user
    When I grant the auth permission to the user with id ${other.id}
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth) Not allowed to grant permission auth |