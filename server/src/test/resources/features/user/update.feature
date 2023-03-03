Feature: Update users

  Background:
    Given there is a user user with
      | name     | Test user       |
      | username | user@apized.org |
      | password | user_password   |

  Scenario: User can update his own user
    Given I login as user
    When I update an user with id ${user.id} with
      | name | Updated user |
    Then the request succeeds
    And the response contains
      | name | Updated user |

  Scenario: Anonymous can't update other users
    Given I login as anonymous
    When I update an user with id ${user.id} with
      | name | Updated user |
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.user.get.${user.id}) Not allowed to get User with id ${user.id} |

  Scenario: Admin can update other users
    Given I login as administrator
    When I update an user with id ${user.id} with
      | name | Updated user |
    Then the request succeeds
    And the response contains
      | name | Updated user |

  Scenario: Admin can verify users via update
    Given I login as administrator
    And I get a user with id ${user.id}
    And the response contains
      | verified | false |
    When I update an user with id ${user.id} with
      | verified | true |
    Then the request succeeds
    And the response contains
      | verified | true |

  Scenario: Anonymous cannot verify users via update
    Given I login as anonymous
    When I update an user with id ${user.id} with
      | verified | true |
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.user.get.${user.id}) Not allowed to get User with id ${user.id} |

  Scenario Outline: Validations
    Given I login as administrator
    When I update a user with id ${user.id} with
      | <field> | <value> |
    Then the request fails
    And the response path "errors" contains element with
      | field   | <field> |
      | message | <error> |
    Examples:
      | field    | value | error            |
      | name     |       | must not be null |
      | username |       | must not be null |