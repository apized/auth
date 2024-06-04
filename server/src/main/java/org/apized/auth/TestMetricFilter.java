package org.apized.auth;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micronaut.configuration.metrics.annotation.RequiresMetrics;
import io.micronaut.configuration.metrics.binder.web.WebMetricsPublisher;
import io.micronaut.configuration.metrics.binder.web.WebMetricsServerCondition;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.annotation.ResponseFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.http.uri.UriMatchTemplate;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.web.router.UriRouteInfo;
import io.micronaut.web.router.UriRouteMatch;
import jakarta.inject.Inject;
import org.apized.micronaut.core.ApizedServerFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static io.micronaut.core.util.StringUtils.FALSE;
import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static io.micronaut.http.HttpStatus.OK;

/**
 * Registers the timers and meters for each request.
 *
 * <p>The default is to intercept all paths /**, but using the
 * property micronaut.metrics.http.path, this can be changed.</p>
 *
 * @author Christian Oestreich
 * @author graemerocher
 * @since 1.0
 */
@ServerFilter(Filter.MATCH_ALL_PATTERN)
@RequiresMetrics
@Requires(property = WebMetricsPublisher.ENABLED, notEquals = FALSE)
@Requires(condition = WebMetricsServerCondition.class)
public class TestMetricFilter extends ApizedServerFilter {
  @Inject
  ObjectMapper mapper;

  @Inject
  MeterRegistry meterRegistry;

  private static final String UNMATCHED_URI = "UNMATCHED_URI";

  private String resolvePath(HttpRequest<?> request) {
    Optional<String> routeInfo = request.getAttribute(HttpAttributes.ROUTE_INFO, UriRouteMatch.class)
      .map(UriRouteMatch::getRouteInfo)
      .map(UriRouteInfo::getUriMatchTemplate)
      .map(UriMatchTemplate::toPathString);
    return routeInfo.orElseGet(() -> request.getAttribute(HttpAttributes.URI_TEMPLATE, String.class)
      .orElse(UNMATCHED_URI));
  }

  @ResponseFilter
  @ExecuteOn(TaskExecutors.BLOCKING)
  public void responseFilter(HttpRequest<?> request, MutableHttpResponse<?> response) {
    if (shouldExclude(request.getPath())) return;

    try {
      long bytes = response.getBody().isPresent() ? Optional.of(this.mapper.writeValueAsBytes(response.getBody().get()).length).orElse(0) : 0L;
      String httpMethod = request.getMethodName();
      String requestPath = resolvePath(request);
      String serviceId = resolveServiceID(request);
      meterRegistry.summary(
        "http.server.requests.bytes",
        Stream.of(
            httpMethod == null ? null : Tag.of(METHOD, httpMethod),
            status(response),
            uri(response, requestPath),
            serviceId == null ? null : Tag.of(SERVICE_ID, serviceId)
          )
          .filter(Objects::nonNull)
          .toList()
      ).record(bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  static final String UNKNOWN = "UNKNOWN";
  private static final Tag URI_NOT_FOUND = Tag.of("uri", "NOT_FOUND");
  private static final Tag URI_REDIRECTION = Tag.of("uri", "REDIRECTION");
  private static final String METHOD = "method";
  private static final String STATUS = "status";
  private static final String URI = "uri";
  private static final String SERVICE_ID = "serviceId";

  /**
   * Get a tag with the HTTP status value.
   *
   * @param httpResponse the HTTP response
   * @return Tag of status
   */
  private static Tag status(HttpResponse<?> httpResponse) {
    if (httpResponse == null) {
      return Tag.of(STATUS, "500");
    }

    HttpStatus status = httpResponse.status();
    if (status == null) {
      status = OK;
    }
    return Tag.of(STATUS, String.valueOf(status.getCode()));
  }

  /**
   * Get a tag with the URI.
   *
   * @param httpResponse the HTTP response
   * @param path         the path of the request
   * @return Tag of URI
   */
  private static Tag uri(HttpResponse<?> httpResponse, String path) {
    if (httpResponse != null) {
      HttpStatus status = httpResponse.getStatus();
      if (status != null && status.getCode() >= 300 && status.getCode() < 400) {
        return URI_REDIRECTION;
      }
      if (status != null && status.equals(NOT_FOUND)) {
        return URI_NOT_FOUND;
      }
    }
    return Tag.of(URI, sanitizePath(path));
  }

  private static String sanitizePath(String path) {
    if (!StringUtils.isEmpty(path)) {
      path = path
        .replaceAll("//+", "/")
        .replaceAll("/$", "");
    }

    return path != null ? (path.isEmpty() ? "root" : path) : UNKNOWN;
  }

  @SuppressWarnings("java:S2259") // false positive
  private String resolveServiceID(HttpRequest<?> request) {
    String serviceId = request.getAttributes().get(HttpAttributes.SERVICE_ID.toString(), String.class).orElse(null);
    if (StringUtils.isNotEmpty(serviceId) && serviceId.charAt(0) == '/') {
      return "embedded-server";
    }
    return serviceId;
  }

  @Override
  public int getOrder() {
    return ServerFilterPhase.LAST.before();
  }
}
