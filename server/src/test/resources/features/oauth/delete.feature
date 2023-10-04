Feature: Delete Oauth

  Background:
    Given there is an oauth named "Google" as oauth

  Scenario: Admin user can delete oauth
    Given I login as administrator
    And I list the oauths
    And the response path "content" contains 1 elements
    When I delete an oauth with id ${oauth.id}
    Then the request succeeds
    And I list the oauths
    And the response path "content" contains 0 elements

  Scenario: Anonymous user can't delete oauth
    Given I login as anonymous
    And I list the oauths
    And the response path "content" contains 1 elements
    When I delete an oauth with id ${oauth.id}
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.oauth.delete.${oauth.id}) Not allowed to delete Oauth with id ${oauth.id} |
    And I list the oauths
    And the response path "content" contains 1 elements

