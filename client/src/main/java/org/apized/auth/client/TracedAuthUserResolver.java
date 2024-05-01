package org.apized.auth.client;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import org.apized.core.security.model.User;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import java.util.function.Supplier;

@Requires(bean = Tracer.class)
@Replaces(AuthUserResolver.class)
@Singleton
public class TracedAuthUserResolver extends AuthUserResolver {

  private final Tracer tracer;

  public TracedAuthUserResolver(Tracer tracer) {
    super();
    this.tracer = tracer;
  }

  @Override
  public User getUser(String token) {
    return tracedRequest(
      config.getFederation().get("auth") + "/tokens/" + token + "?fields=*,roles.id,roles.name,roles.permissions",
      () -> super.getUser(token)
    );
  }

  @Override
  public User getUser(UUID userId) {
    return tracedRequest(
      config.getFederation().get("auth") + "/users/" + userId + "?fields=*,roles.id,roles.name,roles.permissions",
      () -> super.getUser(userId)
    );
  }

  @Override
  public String generateToken(User user, boolean expiring) {
    return tracedRequest(
      config.getFederation().get("auth") + "/auth/users/" + user.getId() + "/token?expiring=" + expiring,
      () -> super.generateToken(user, expiring)
    );
  }

  @SneakyThrows
  protected <T> T tracedRequest(String url, Supplier<T> supplier) {
    Span span = tracer
      .spanBuilder(url)
      .setSpanKind(SpanKind.CLIENT)
      .setAttribute("http.method", "GET")
      .setAttribute("http.url", url)
      .startSpan();

    try (Scope ignore = span.makeCurrent()) {
      return supplier.get();
    } catch (Throwable t) {
      span.setStatus(StatusCode.ERROR);
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      span.recordException(
        t,
        Attributes.of(
          AttributeKey.booleanKey("exception.escaped"), true,
          AttributeKey.stringKey("exception.message"), t.getMessage(),
          AttributeKey.stringKey("exception.stacktrace"), sw.toString(),
          AttributeKey.stringKey("exception.type"), t.getClass().getName()
        )
      );
      throw t;
    } finally {
      span.end();
    }
  }
}
