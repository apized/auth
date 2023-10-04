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
      | name           | TestAuth                |
      | slug           | oauth-1                 |
      | loginUrl       | https://loginUrl.org    |
      | accessTokenUrl | https://accessToken.org |
      | properties     | [ : ]                   |
      | mapping        | [ : ]                   |
      | userUrl        | https://loginUrl.org    |
      | userHeaders    | [ : ]                   |
      | emailUrl       | https://emailUrl.org    |
      | emailHeaders   | [ : ]                   |
    Then the request succeeds
    And the response contains
      | name           | TestAuth                |
      | slug           | oauth-1                 |
      | loginUrl       | https://loginUrl.org    |
      | accessTokenUrl | https://accessToken.org |
      | properties     | [ : ]                   |
      | mapping        | [ : ]                   |
      | userUrl        | https://loginUrl.org    |
      | userHeaders    | [ : ]                   |
      | emailUrl       | https://emailUrl.org    |
      | emailHeaders   | [ : ]                   |

  Scenario: Anonymous user can't create an Oauth
    When I create an oauth with
      | name           | TestAuth                |
      | slug           | oauth-1                 |
      | loginUrl       | https://loginUrl.org    |
      | accessTokenUrl | https://accessToken.org |
      | properties     | [ : ]                   |
      | mapping        | [ : ]                   |
      | userUrl        | https://loginUrl.org    |
      | userHeaders    | [ : ]                   |
      | emailUrl       | https://emailUrl.org    |
      | emailHeaders   | [ : ]                   |
    Then the request fails
    And the response path "errors" contains element with
      | message | /.*Not allowed to create Oauth.*/ |

  Scenario: Creating an existing oauth should not be valid
    Given I login as administrator
    And I create an oauth with
      | name           | TestAuth                |
      | slug           | oauth-1                 |
      | loginUrl       | https://loginUrl.org    |
      | accessTokenUrl | https://accessToken.org |
      | properties     | [ : ]                   |
      | mapping        | [ : ]                   |
      | userUrl        | https://loginUrl.org    |
      | userHeaders    | [ : ]                   |
      | emailUrl       | https://emailUrl.org    |
      | emailHeaders   | [ : ]                   |
    When I create an oauth with
      | name           | TestAuth                |
      | slug           | oauth-1                 |
      | loginUrl       | https://loginUrl.org    |
      | accessTokenUrl | https://accessToken.org |
      | properties     | [ : ]                   |
      | mapping        | [ : ]                   |
      | userUrl        | https://loginUrl.org    |
      | userHeaders    | [ : ]                   |
      | emailUrl       | https://emailUrl.org    |
      | emailHeaders   | [ : ]                   |
    Then the request fails
    And the response path "errors" contains element with
      | message | DataAccessException: Error executing PERSIST: ERROR: duplicate key value violates unique constraint "oauth_slug_key"\n  Detail: Key (slug)=(oauth-1) already exists. |

  Scenario Outline: Oauth validation
    Given I login as administrator
    When I create an oauth with
      | name           | <name>           |
      | slug           | <slug>           |
      | loginUrl       | <loginUrl>       |
      | accessTokenUrl | <accessTokenUrl> |
      | properties     | <properties>     |
      | mapping        | <mapping>        |
      | userUrl        | <userUrl>        |
      | userHeaders    | <userHeaders>    |
      | emailUrl       | <emailUrl>       |
      | emailHeaders   | <emailHeaders>   |
    Then the request <result>
    And the response path "<path>" contains
      | <field> | <value> |
    Examples:
      | name     | slug                 | loginUrl             | accessTokenUrl          | properties | mapping | userUrl              | userHeaders | emailUrl             | emailHeaders | result   | path     | field          | value                                 |
      #name
      |          | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | must not be null                      |
      | T        | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | size must be between 3 and 2147483647 |
      | Te       | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | size must be between 3 and 2147483647 |
      | Tes      | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | succeeds | _        | name           | Tes                                   |
      #slug
      | TestAuth |                      | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | must not be null                      |
      | TestAuth | o                    | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | size must be between 3 and 15         |
      | TestAuth | oa                   | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | size must be between 3 and 15         |
      | TestAuth | oau                  | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | succeeds | _        | slug           | oau                                   |
      | TestAuth | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | succeeds | _        | slug           | oauth-1                               |
      | TestAuth | oauth_1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | must match "[a-z0-9-]+"               |
      | TestAuth | oauth:1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | must match "[a-z0-9-]+"               |
      | TestAuth | oauth-12345678912345 | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | size must be between 3 and 15         |
      #loginUrl
      | TestAuth | oauth-1              |                      | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | must not be null                      |
      | TestAuth | oauth-1              | http://              | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | must match "https://.+"               |
      | TestAuth | oauth-1              | htt://loginUrl.org   | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | must match "https://.+"               |
      | TestAuth | oauth-1              | http://loginUrl.org  | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | must match "https://.+"               |
      | TestAuth | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | succeeds | _        | loginUrl       | https://loginUrl.org                  |
      #accessTokenUrl
      | TestAuth | oauth-1              | https://loginUrl.org |                         | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | /.* null value in column .*/          |
      | TestAuth | oauth-1              | https://loginUrl.org | https://                | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | must match "https://.+"               |
      | TestAuth | oauth-1              | https://loginUrl.org | http://accessToken.org  | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | must match "https://.+"               |
      | TestAuth | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | succeeds | _        | accessTokenUrl | https://accessToken.org               |
      #userUrl
      | TestAuth | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://             | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | must match "^(\|https://.+)\\\\\\$"   |
      | TestAuth | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | htt://loginUrl.org   | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | must match "^(\|https://.+)\\\\\\$"   |
      | TestAuth | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | http://loginUrl.org  | [ : ]       | https://emailUrl.org | [ : ]        | fails    | errors.0 | message        | must match "^(\|https://.+)\\\\\\$"   |
      | TestAuth | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | succeeds | _        | userUrl        | https://loginUrl.org                  |
      | TestAuth | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   |                      | [ : ]       | https://emailUrl.org | [ : ]        | succeeds | _        | userUrl        |                                       |
      #emailUrl
      | TestAuth | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://             | [ : ]        | fails    | errors.0 | message        | must match "^(\|https://.+)\\\\\\$"   |
      | TestAuth | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | http://emailUrl.org  | [ : ]        | fails    | errors.0 | message        | must match "^(\|https://.+)\\\\\\$"   |
      | TestAuth | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       | https://emailUrl.org | [ : ]        | succeeds | _        | emailUrl       | https://emailUrl.org                  |
      | TestAuth | oauth-1              | https://loginUrl.org | https://accessToken.org | [ : ]      | [ : ]   | https://loginUrl.org | [ : ]       |                      | [ : ]        | succeeds | _        | emailUrl       |                                       |
