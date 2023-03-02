Feature: Login (create token)

  Background:
    Given there is a user test with
      | username    | test@apized.org |
      | password    | test_password   |
      | name        | Test user       |
      | permissions | ['a.a']         |
      | verified    | true            |

  Scenario: Can login with correct username/password pairs
    When I login with username test@apized.org and password test_password
    Then the request succeeds
    And the response contains
      | jwt | /.*/ |

  Scenario: Can't login with invalid password
    When I login with username test@apized.org and password wrong_password
    Then the request fails
    And the response path "errors" contains element with
      | message | Not authorized |

  Scenario: Can't login with non-existing username
    When I login with username invalid@apized.org and password test_password
    Then the request fails
    And the response path "errors" contains element with
      | message | Not authorized |

  Scenario: Can't login with invalid username
    When I login with username invalid and password test_password
    Then the request fails
    And the response path "errors" contains element with
      | message | Not authorized |

  Scenario: Can't login with un-verified user
    Given there is a user test with
      | username | unverified@apized.org |
      | password | unverified_password   |
      | name     | Test user             |
    When I login with username unverified@apized.org and password unverified_password
    Then the request fails
    And the response path "errors" contains element with
      | message | Email verification pending |