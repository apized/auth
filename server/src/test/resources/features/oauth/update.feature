Feature: Oauth update

  Background:
    Given there is an oauth named "AuthTest" as oauth

  Scenario: Admin can update an existing oauth
    Given I login as administrator
    When I update an oauth with id ${oauth.id} with
      | name           | NewName                        |
      | slug           | oauth-changed                  |
      | loginUrl       | https://alteredLoginUrl.org    |
      | accessTokenUrl | https://alteredAccessToken.org |
      | properties     | [ prop1 : 'propVal1' ]         |
      | mapping        | [ map1 : 'mapVal1' ]           |
      | userUrl        | https://alteredUserUrl.org     |
      | userHeaders    | [ user1: 'userVal1' ]          |
      | emailUrl       | https://alteredEmailUrl.org    |
      | emailHeaders   | [ email1: 'emailVal1' ]        |
    Then the request succeeds
    And the response contains
      | name           | NewName                        |
      | slug           | oauth-changed                  |
      | loginUrl       | https://alteredLoginUrl.org    |
      | accessTokenUrl | https://alteredAccessToken.org |
      | properties     | [ prop1 : 'propVal1' ]         |
      | mapping        | [ map1 : 'mapVal1' ]           |
      | userUrl        | https://alteredUserUrl.org     |
      | userHeaders    | [ user1: 'userVal1' ]          |
      | emailUrl       | https://alteredEmailUrl.org    |
      | emailHeaders   | [ email1: 'emailVal1' ]        |

  Scenario: Anonymous user can't update an existing oauth
    When I update an oauth with id ${oauth.id} with
      | name           | NewName                        |
      | slug           | oauth-changed                  |
      | loginUrl       | https://alteredLoginUrl.org    |
      | accessTokenUrl | https://alteredAccessToken.org |
      | properties     | [ prop1 : 'propVal1' ]         |
      | mapping        | [ map1 : 'mapVal1' ]           |
      | userUrl        | https://alteredUserUrl.org     |
      | userHeaders    | [ user1: 'userVal1' ]          |
      | emailUrl       | https://alteredEmailUrl.org    |
      | emailHeaders   | [ email1: 'emailVal1' ]        |
    Then the request fails
    And the response path "errors" contains element with
      | message | /.*Not allowed to update Oauth.*/ |
