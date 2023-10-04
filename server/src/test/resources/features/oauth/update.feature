Feature: Oauth update

  Background:
    Given there is an oauth named "AuthTest" as oauth

  Scenario: Admin can update an existing oauth
    Given I login as administrator
    When I update an oauth with id ${oauth.id} with
      | name         | NewName      |
      | slug         | test-changed |
      | provider     | Facebook     |
      | clientId     | newClient    |
      | clientSecret | newSecret    |
    Then the request succeeds
    And the response contains
      | name         | NewName      |
      | slug         | test-changed |
      | provider     | Facebook     |
      | clientId     | newClient    |
      | clientSecret | newSecret    |

  Scenario: Anonymous user can't update an existing oauth
    When I update an oauth with id ${oauth.id} with
      | name         | NewName      |
      | slug         | test-changed |
      | provider     | Facebook     |
      | clientId     | newClient    |
      | clientSecret | newSecret    |
    Then the request fails
    And the response path "errors" contains element with
      | message | /.*Not allowed to update Oauth.*/ |
