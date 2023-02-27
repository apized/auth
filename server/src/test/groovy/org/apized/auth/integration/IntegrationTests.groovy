package org.apized.auth.integration

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.runner.RunWith

@MicronautTest
@RunWith(Cucumber)
@CucumberOptions(
  plugin = ["pretty", "html:target/features"],
  features = ["src/test/resources/features"],
  glue = ["org.apized"]
)
class IntegrationTests {}
