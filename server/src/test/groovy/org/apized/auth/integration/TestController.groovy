/*
 * Copyright 2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apized.auth.integration

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import jakarta.inject.Inject
import org.apized.auth.api.user.User
import org.apized.auth.api.user.UserService
import org.apized.micronaut.test.integration.MicronautTestController

import jakarta.transaction.Transactional

@Transactional
@Controller('/integration')
class TestController extends MicronautTestController {

  @Inject
  UserService userService

  @Get('/users/{userId}/emailVerificationCode')
  HttpResponse<String> getEmailVerificationCode(UUID userId) {
    User user = userService.get(userId)
    HttpResponse.ok(user.getEmailVerificationCode())
  }

  @Get('/users/{userId}/passwordResetCode')
  HttpResponse<String> getPasswordResetCode(UUID userId) {
    User user = userService.get(userId)
    HttpResponse.ok(user.getPasswordResetCode())
  }
}
