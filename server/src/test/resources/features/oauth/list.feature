Feature: List oauth

  Background:
    Given I login as administrator
    And I create an oauth with
      | name           | TestAuth                |
      | slug           | oauth-1                 |
      | loginUrl       | https://loginUrl.org    |
      | accessTokenUrl | https://accessToken.org |
      | properties     | [ : ]                   |
      | mapping        | [ : ]                   |
      | userUrl        | https://userUrl.org     |
      | userHeaders    | [ : ]                   |
      | emailUrl       | https://emailUrl.org    |
      | emailHeaders   | [ : ]                   |
    And I login as anonymous

  Scenario: Admin can list Oauths
    Given I login as administrator
    When I list the oauths
    Then the request succeeds
    And the response path "content" contains 1 elements

  Scenario: Anonymous user can list Oauths
    Given I login as anonymous
    When I list the oauths
    Then the request succeeds
    And the response path "content" contains 1 elements


