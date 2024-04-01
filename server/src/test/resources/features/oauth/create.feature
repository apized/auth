Feature: Oauth configuration creation

  Scenario: Admin can create an Oauth
    Given I login as administrator
    And I create a role as role with
      | name | TestRole |
    And I create a user as john with
      | name     | John                     |
      | username | john.doe@test.com        |
      | password | abcdef                   |
      | roles    | [ [ id: '${role.id}' ] ] |
    When I create an oauth with
      | name         | TestAuth |
      | slug         | oauth-1  |
      | provider     | Google   |
      | clientId     | id       |
      | clientSecret | secret   |
    Then the request succeeds
    And the response contains
      | name         | TestAuth |
      | slug         | oauth-1  |
      | provider     | Google   |
      | clientId     | id       |
      | clientSecret | secret   |

  Scenario: Anonymous user can't create an Oauth
    When I create an oauth with
      | name         | TestAuth |
      | slug         | oauth-1  |
      | provider     | Google   |
      | clientId     | id       |
      | clientSecret | secret   |
    Then the request fails
    And the response path "errors" contains element with
      | message | /.*Not allowed to create Oauth.*/ |

  Scenario: Creating an existing oauth should not be valid
    Given I login as administrator
    And I create an oauth with
      | name         | TestAuth |
      | slug         | oauth-1  |
      | provider     | Google   |
      | clientId     | id       |
      | clientSecret | secret   |
    When I create an oauth with
      | name         | TestAuth |
      | slug         | oauth-1  |
      | provider     | Google   |
      | clientId     | id       |
      | clientSecret | secret   |
    Then the request fails
    And the response path "errors" contains element with
      | message | DataAccessException: Error executing PERSIST: ERROR: duplicate key value violates unique constraint "oauth_slug_key"\n  Detail: Key (slug)=(oauth-1) already exists. |

  Scenario Outline: Oauth validation
    Given I login as administrator
    When I create an oauth with
      | name         | <name>         |
      | slug         | <slug>         |
      | provider     | <provider>     |
      | clientId     | <clientId>     |
      | clientSecret | <clientSecret> |
    Then the request <result>
    And the response path "<path>" contains
      | <field> | <value> |
    Examples:
      | name     | slug                    | provider | clientId | clientSecret | result   | path     | field        | value                                                      |
      #name
      |          | test                    | Google   | client   | secret       | fails    | errors.0 | message      | must not be null                                           |
      | T        | test                    | Google   | client   | secret       | fails    | errors.0 | message      | size must be between 3 and 2147483647                      |
      | Te       | test                    | Google   | client   | secret       | fails    | errors.0 | message      | size must be between 3 and 2147483647                      |
      | Tes      | test                    | Google   | client   | secret       | succeeds | _        | name         | Tes                                                        |
      #slug
      | TestAuth |                         | Google   | client   | secret       | fails    | errors.0 | message      | must not be null                                           |
      | TestAuth | t                       | Google   | client   | secret       | fails    | errors.0 | message      | size must be between 3 and 15                              |
      | TestAuth | te                      | Google   | client   | secret       | fails    | errors.0 | message      | size must be between 3 and 15                              |
      | TestAuth | tes                     | Google   | client   | secret       | succeeds | _        | slug         | tes                                                        |
      | TestAuth | test                    | Google   | client   | secret       | succeeds | _        | slug         | test                                                       |
      | TestAuth | test-123456789123456789 | Google   | client   | secret       | fails    | errors.0 | message      | size must be between 3 and 15                              |
      #provider
      | TestAuth | test                    |          | client   | secret       | fails    | errors.0 | message      | must not be null                                           |
      | TestAuth | test                    | Fruit    | client   | secret       | fails    | errors.0 | message      | No enum constant org.apized.auth.oauth.OauthProvider.Fruit |
      | TestAuth | test                    | Google   | client   | secret       | succeeds | _        | provider     | Google                                                     |
      #clientId
      | TestAuth | test                    | Google   |          | secret       | fails    | errors.0 | message      | must not be null                                           |
      | TestAuth | test                    | Google   | client   | secret       | succeeds | _        | clientId     | client                                                     |
      #clientSecret
      | TestAuth | test                    | Google   | client   |              | fails    | errors.0 | message      | must not be null                                           |
      | TestAuth | test                    | Google   | client   | secret       | succeeds | _        | clientSecret | secret                                                     |
