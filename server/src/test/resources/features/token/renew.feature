Feature: Renew token

  Background:
    Given there is a user user with
      | username | user@apized.org |
      | password | user_password   |
      | name     | Test user       |
      | verified | true            |

  Scenario: Admin can renew valid token
    Given There is a token for user user as token valid for 10 seconds
    And I login as administrator
    When I renew the token ${token.jwt}
    Then the request succeeds
    And the response contains
      | jwt | /.*/ |

  Scenario: Anonymous can renew his tokens
    Given There is a token for user user as token valid for 10 seconds
    And I login as user
    When I renew the token ${token.jwt}
    Then the request succeeds
    And the response contains
      | jwt | /.*/ |

  Scenario: Anonymous can't renew someone else's tokens
    Given There is a token for user user as token valid for 10 seconds
    And I login as anonymous
    When I renew the token ${token.jwt}
    Then the request fails
    And the response path "errors" contains element with
      | message | (auth.token.renew) Not allowed to renew tokens for other users |

  Scenario Outline: Admin can't renew invalid token: <description> (invalid content)
    And I login as administrator
    When I renew the token <token>
    Then the request fails
    And the response path "errors" contains element with
      | message | <message> |
    Examples:
      | description | message                                                           | token                                                                                                                                                                                     |
      | missing aud | MissingClaimException: The Claim 'aud' is not present in the JWT. | eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4MzMwOGJlMi1jOWI2LTQ0YzQtOGYzOC1iYjBmZGIxMDUxN2EiLCJpc3MiOiJhcGl6ZWQiLCJpYXQiOjE2Nzc3NzE5NTR9.gbdfJBzKBQl1es7Kbrio_3f3qRLLDfzhFElBc8Rycp4 |
      | missing sub | MissingClaimException: The Claim 'sub' is not present in the JWT. | eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhcGl6ZWQiLCJpc3MiOiJhcGl6ZWQiLCJpYXQiOjE2Nzc3NzE5NTR9.aHvJtR1PpnGc6PE3BL5k7AmPlSjRIl3RiJKhGo02nyU                                         |
      | missing iss | MissingClaimException: The Claim 'iss' is not present in the JWT. | eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhcGl6ZWQiLCJzdWIiOiI4MzMwOGJlMi1jOWI2LTQ0YzQtOGYzOC1iYjBmZGIxMDUxN2EiLCJpYXQiOjE2Nzc3NzE5NTR9.vd3sakTQESEElcphs3aHC2z2EhGNhg8Orit8PTngDiY |
      | missing iat | MissingClaimException: The Claim 'iat' is not present in the JWT. | eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhcGl6ZWQiLCJzdWIiOiI4MzMwOGJlMi1jOWI2LTQ0YzQtOGYzOC1iYjBmZGIxMDUxN2EiLCJpc3MiOiJhcGl6ZWQifQ.4ko30nagE5EI0a_DDSxrSQ-uPSlWggyeOzjqYyEfm6A   |

  Scenario: Admin can't renew invalid token (invalid signature)
    And I login as administrator
    When I renew the token eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhcGl6ZWQiLCJzdWIiOiI4MzMwOGJlMi1jOWI2LTQ0YzQtOGYzOC1iYjBmZGIxMDUxN2EiLCJpc3MiOiJhcGl6ZWQiLCJleHAiOjE2Nzc3NzE5NjQsImlhdCI6MTY3Nzc3MTk1NCwianRpIjoiMWU2ZDFjNDItNmRjNy00MmQyLTk1ZDItYzJmYmM1MmQ0MTVhIn0.lIhYSjPGILRBmPs8w2ep_HlWHd926nBoKfgsWmHDe8o
    Then the request fails
    And the response path "errors" contains element with
      | message | SignatureVerificationException: The Token's Signature resulted invalid when verified using the Algorithm: HmacSHA256 |

  Scenario: Can't renew expired token
    Given There is a token for user user as token valid for 0 seconds
    And I login as administrator
    When I renew the token ${token.jwt}
    Then the request fails
    And the response path "errors" contains element with
      | message | /TokenExpiredException: The Token has expired on .*/ |