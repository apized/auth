Feature: API key lifecycle

  Background:
    Given there is a user keyOwner with
      | name        | Key Owner             |
      | username    | keyOwner@apized.org   |
      | password    | secure_password       |
      | verified    | true                  |
      | permissions | [ 'auth.user.list' ] |
    And I login as keyOwner

  Scenario: An owner creates an API key and receives its secret only once
    When I create an API key named "Deployment" as apiKey
    Then the request succeeds
    And the apiKey response contains
      | name | Deployment |
      | key  | /ak_.*/    |
    When I get the API key ${apiKey.id}
    Then the request succeeds
    And the response matches apikey/without-secret.json
    When I list API keys
    Then the request succeeds
    And the response matches apikey/page-without-secret.json

  Scenario: Deleting an API key removes its generated record
    When I create an API key named "Deployment" as apiKey
    Then the request succeeds
    When I delete the API key ${apiKey.id}
    Then the request succeeds
    When I get the API key ${apiKey.id}
    Then the request fails
