Feature: Generate token

  Background:
    Given there is a user user with
      | username | user@apized.org |
      | password | user_password   |
      | name     | Test user       |
      | verified | true            |
    And there is a user other with
      | username | other@apized.org |
      | password | other_password   |
      | name     | Other user       |
      | verified | true             |

  Scenario: Admin can generate tokens for himself
    Given I login as administrator
    When I create an expiring token for administrator
    Then the request succeeds
    And the response contains
      | jwt | /.*/ |

  Scenario: Admin can generate non expiring tokens for himself
    Given I login as administrator
    When I create an non-expiring token for administrator
    Then the request succeeds
    And the response contains
      | jwt | /.*/ |

  Scenario: Admin can generate tokens for other users
    Given I login as administrator
    When I create an expiring token for user
    Then the request succeeds
    And the response contains
      | jwt | /.*/ |

  Scenario: Admin can generate non expiring tokens for other users
    Given I login as administrator
    When I create an non-expiring token for user
    Then the request succeeds
    And the response contains
      | jwt | /.*/ |

  Scenario: User can generate tokens for himself
    Given I login as user
    When I create an expiring token for user
    Then the request succeeds
    And the response contains
      | jwt | /.*/ |

  Scenario: User can't generate non expiring tokens for himself
    Given I login as user
    When I create an non-expiring token for user
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.token.create) Not allowed to generate non-expiring tokens for other users |

  Scenario: User can't generate tokens for other users
    Given I login as user
    When I create an expiring token for other
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.token.create) Not allowed to generate non-expiring tokens for other users |

  Scenario: User can't generate non expiring tokens for other users
    Given I login as user
    When I create an non-expiring token for other
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.token.create) Not allowed to generate non-expiring tokens for other users |
