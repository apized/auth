package org.apized.auth;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(
  info = @Info(
    title = "Auth API",
    version = "0.0.1",
    description = """
# Introduction

Ahahah

# Things

BLABLA
"""
  ),
  tags = {
    @Tag(name = "Login", description = "The login tag"),
    @Tag(name = "User", description = "The user tag"),
    @Tag(name = "Role", description = "The role tag")
  }
)
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class Application {
  public static void main(String[] args) {
    Micronaut.run(Application.class, args);
  }
}
