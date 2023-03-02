Feature: Get users

  Background:
    Given there is a user user with
      | name     | Test user       |
      | username | user@apized.org |
      | password | user_password   |
    Given there is a user other with
      | name     | Other user       |
      | username | other@apized.org |
      | password | other_password   |

  Scenario: Anonymous can get himself
    Given I login as user
    When I get an user with id ${user.id}
    Then the request succeeds
    And the response contains
      | name     | Test user       |
      | username | user@apized.org |

  Scenario: Anonymous can't get other users
    Given I login as user
    When I get a user with id ${other.id}
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.user.get.${other.id}) Not allowed to get User with id ${other.id} |

  Scenario: Admin can get other users
    Given I login as administrator
    When I get a user with id ${other.id}
    Then the request succeeds
    And the response contains
      | username | other@apized.org |