Feature: Redeem token

  Background:
    Given there is a user user with
      | username | user@apized.org |
      | password | user_password   |
      | name     | Test user       |
      | verified | true            |
    Given there is a user unverified with
      | username | unverified@apized.org |
      | password | unverified_password   |
      | name     | Unverified user       |

  Scenario: Admin can redeem valid token
    Given There is a token for user user as token valid for 10 seconds
    And I login as administrator
    When I redeem the token ${token.jwt}
    Then the request succeeds
    And the response contains
      | name     | Test user       |
      | username | user@apized.org |


  Scenario: Admin can redeem token for invalid user id (getting the anonymous user)
    Given There is a token for user user as token valid for 10 seconds
    And I login as administrator
    When I redeem the token eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhcGl6ZWQiLCJzdWIiOiI4MzMwOGJlMi1jOWI2LTQ0YzQtOGYzOC1iYjBmZGIxMDUxN2EiLCJpc3MiOiJhcGl6ZWQiLCJpYXQiOjE2Nzc3NzE5NTQsImp0aSI6IjFlNmQxYzQyLTZkYzctNDJkMi05NWQyLWMyZmJjNTJkNDE1YSJ9.WhgM4zBYP3PYs4z9OqAlGYo4GOSgkfG9IwKcNf9lwRA
    Then the request succeeds
    And the response contains
      | name     | Anonymous            |
      | username | anonymous@apized.org |

  Scenario: Admin can redeem token for unverified user
    Given There is a token for user unverified as token valid for 10 seconds
    And I login as administrator
    When I redeem the token ${token.jwt}
    Then the request succeeds
    And the response contains
      | name     | Unverified user       |
      | username | unverified@apized.org |

  Scenario: Anonymous cannot redeem tokens
    Given There is a token for user user as token valid for 10 seconds
    And I login as anonymous
    When I redeem the token ${token.jwt}
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.token.redeem) Not allowed to redeem tokens |

  Scenario Outline: Admin can't redeem invalid token: <description> (invalid content)
    And I login as administrator
    When I redeem the token <token>
    Then the request fails
    And the response path "errors" contains element with
      | message | <message> |
    Examples:
      | description | message                                                           | token                                                                                                                                                                                     |
      | missing aud | MissingClaimException: The Claim 'aud' is not present in the JWT. | eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4MzMwOGJlMi1jOWI2LTQ0YzQtOGYzOC1iYjBmZGIxMDUxN2EiLCJpc3MiOiJhcGl6ZWQiLCJpYXQiOjE2Nzc3NzE5NTR9.gbdfJBzKBQl1es7Kbrio_3f3qRLLDfzhFElBc8Rycp4 |
      | missing sub | MissingClaimException: The Claim 'sub' is not present in the JWT. | eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhcGl6ZWQiLCJpc3MiOiJhcGl6ZWQiLCJpYXQiOjE2Nzc3NzE5NTR9.aHvJtR1PpnGc6PE3BL5k7AmPlSjRIl3RiJKhGo02nyU                                         |
      | missing iss | MissingClaimException: The Claim 'iss' is not present in the JWT. | eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhcGl6ZWQiLCJzdWIiOiI4MzMwOGJlMi1jOWI2LTQ0YzQtOGYzOC1iYjBmZGIxMDUxN2EiLCJpYXQiOjE2Nzc3NzE5NTR9.vd3sakTQESEElcphs3aHC2z2EhGNhg8Orit8PTngDiY |
      | missing iat | MissingClaimException: The Claim 'iat' is not present in the JWT. | eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhcGl6ZWQiLCJzdWIiOiI4MzMwOGJlMi1jOWI2LTQ0YzQtOGYzOC1iYjBmZGIxMDUxN2EiLCJpc3MiOiJhcGl6ZWQifQ.4ko30nagE5EI0a_DDSxrSQ-uPSlWggyeOzjqYyEfm6A   |

  Scenario: Admin can't redeem invalid token (invalid signature)
    And I login as administrator
    When I redeem the token eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhcGl6ZWQiLCJzdWIiOiI4MzMwOGJlMi1jOWI2LTQ0YzQtOGYzOC1iYjBmZGIxMDUxN2EiLCJpc3MiOiJhcGl6ZWQiLCJleHAiOjE2Nzc3NzE5NjQsImlhdCI6MTY3Nzc3MTk1NCwianRpIjoiMWU2ZDFjNDItNmRjNy00MmQyLTk1ZDItYzJmYmM1MmQ0MTVhIn0.lIhYSjPGILRBmPs8w2ep_HlWHd926nBoKfgsWmHDe8o
    Then the request fails
    And the response path "errors" contains element with
      | message | SignatureVerificationException: The Token's Signature resulted invalid when verified using the Algorithm: HmacSHA256 |

  Scenario: Admin can't redeem expired token
    Given There is a token for user user as token valid for 0 seconds
    And I login as anonymous
    When I redeem the token ${token.jwt}
    Then the request fails
    And the response path "errors" contains element with
      | message | /TokenExpiredException: The Token has expired on .*/ |

  Scenario: Dummy/garbage token returns anonymous
    Given There is a token for user user as token valid for 10 seconds
    And I login as administrator
    When I renew the token 9ea00145-ade1-4b2f-b159-be3dc184ca1e
    Then the request succeeds
    And the response contains
      | jwt | /.*/ |
