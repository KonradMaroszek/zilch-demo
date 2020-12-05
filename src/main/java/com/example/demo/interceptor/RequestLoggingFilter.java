package com.example.demo.interceptor;

import static net.logstash.logback.marker.Markers.aggregate;
import static org.springframework.web.servlet.DispatcherServlet.EXCEPTION_ATTRIBUTE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.RawJsonAppendingMarker;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

@Slf4j
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingFilter extends OncePerRequestFilter {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String DEFAULT_ERROR =
      DefaultErrorAttributes.class.getName().concat(".ERROR");

  private static final int DEFAULT_MAX_PAYLOAD_LENGTH = 1024;

  @Default private boolean includeQueryString = false;

  @Default private boolean includeClientInfo = false;

  @Default private boolean includeHeaders = false;

  @Default private boolean includePayload = false;

  @Default private int maxPayloadLength = DEFAULT_MAX_PAYLOAD_LENGTH;

  private Collection<Pattern> patterns;

  @Override
  protected boolean shouldNotFilterAsyncDispatch() {
    return false;
  }

  @Override
  protected void doFilterInternal(
      @NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain)
      throws ServletException, IOException {

    String uri = request.getRequestURI();
    if (shouldNotBeLogged(uri)) {
      filterChain.doFilter(request, response);
      return;
    }

    boolean isFirstRequest = !isAsyncDispatch(request);
    HttpServletRequest requestToUse = request;
    HttpServletResponse responseToUse = response;

    if (isIncludePayload()
        && isFirstRequest
        && !(request instanceof ContentCachingRequestWrapper)) {
      requestToUse = new ContentCachingRequestWrapper(request, getMaxPayloadLength());
    }

    if (isIncludePayload()
        && isFirstRequest
        && !(response instanceof ContentCachingResponseWrapper)) {
      responseToUse = new ContentCachingResponseWrapper(response);
    }

    if (isFirstRequest) {
      beforeRequest(requestToUse);
    }

    var start = Instant.now();
    try {
      filterChain.doFilter(requestToUse, responseToUse);
    } finally {
      if (!isAsyncStarted(requestToUse)) {
        var duration = Duration.between(start, Instant.now());
        afterRequest(requestToUse, responseToUse, duration);
      }
    }
    if (responseToUse instanceof ContentCachingResponseWrapper) {
      ((ContentCachingResponseWrapper) responseToUse).copyBodyToResponse();
    }
  }

  /** Extracts the message payload portion of the message */
  @NotNull
  protected String getMessagePayload(HttpServletRequest request) {
    var wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
    if (wrapper != null) {
      byte[] buf = wrapper.getContentAsByteArray();
      if (buf.length > 0) {
        int length = Math.min(buf.length, getMaxPayloadLength());
        try {
          return new String(buf, 0, length, wrapper.getCharacterEncoding());
        } catch (UnsupportedEncodingException ex) {
          return "[unknown]";
        }
      }
    }
    return "[empty]";
  }

  /** Extracts the message payload portion of the message */
  @NotNull
  protected String getMessagePayload(HttpServletResponse response) {
    var wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
    if (wrapper != null) {
      byte[] buf = wrapper.getContentAsByteArray();
      if (buf.length > 0) {
        int length = Math.min(buf.length, getMaxPayloadLength());
        try {
          return new String(buf, 0, length, wrapper.getCharacterEncoding());
        } catch (UnsupportedEncodingException ex) {
          return "[unknown]";
        }
      }
    }
    return "[empty]";
  }

  /** Writes a log message before the request is processed. */
  protected void afterRequest(
      @NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      Duration duration) {
    String uri = request.getRequestURI();

    if (isIncludeQueryString()) {
      String queryString = request.getQueryString();
      if (queryString != null) {
        uri = uri + "?" + queryString;
      }
    }

    var reqMap = new LinkedHashMap<String, Object>();
    var respMap = new LinkedHashMap<String, Object>();

    if (isIncludeClientInfo()) {
      String client = request.getRemoteAddr();
      if (StringUtils.hasLength(client)) {
        reqMap.put("client", client);
      }
      HttpSession session = request.getSession(false);
      if (session != null) {
        reqMap.put("session", session.getId());
      }
      String user = request.getRemoteUser();
      if (user != null) {
        reqMap.put("user", user);
      }
    }

    if (isIncludeHeaders()) {
      reqMap.put("headers", new ServletServerHttpRequest(request).getHeaders());
      respMap.put("headers", new ServletServerHttpResponse(response).getHeaders());
    }

    int status = response.getStatus();
    respMap.put("status", status);

    if (isIncludePayload()) {
      reqMap.put("payload", getMessagePayload(request));
      respMap.put("payload", getMessagePayload(response));
    }

    // Spring will add an error as attribute
    var throwable = asThrowable(request, DEFAULT_ERROR);
    if (throwable == null) {
      // a servlet exception will be added like this
      throwable = asThrowable(request, EXCEPTION_ATTRIBUTE);
    }

    var durMap = new LinkedHashMap<String, Object>();
    durMap.put("text", duration.toString());
    durMap.put("millis", duration.toMillis());
    durMap.put("nanos", duration.toNanos());
    durMap.put("seconds", duration.toSeconds());

    var stat = new RawJsonAppendingMarker("status", Integer.toString(status));
    var req = new RawJsonAppendingMarker("request", getRawJson(reqMap));
    var resp = new RawJsonAppendingMarker("response", getRawJson(respMap));
    var dur = new RawJsonAppendingMarker("duration", getRawJson(durMap));

    switch (status / 100) {
      case 2:
      case 3:
        logger.info(
            aggregate(req, resp, stat, dur),
            "{} {} => {}",
            request.getMethod(),
            uri,
            status,
            throwable);
        break;
      case 5:
        logger.error(
            aggregate(req, resp, stat, dur),
            "{} {} => {}",
            request.getMethod(),
            uri,
            status,
            throwable);
        break;
      default:
        logger.warn(
            aggregate(req, resp, stat, dur),
            "{} {} => {}",
            request.getMethod(),
            uri,
            status,
            throwable);
    }
  }

  @Nullable
  private Throwable asThrowable(@NotNull HttpServletRequest request, @NotNull String attributeKey) {
    var value = request.getAttribute(attributeKey);
    if (value instanceof Throwable) {
      return (Throwable) value;
    }
    return null;
  }

  @SneakyThrows
  private String getRawJson(Map<String, Object> infoMap) {
    return mapper.writeValueAsString(infoMap);
  }

  private boolean shouldNotBeLogged(String uri) {
    if (patterns == null || patterns.isEmpty()) {
      return false;
    }

    for (var pattern : patterns) {
      if (pattern.matcher(uri).matches()) {
        return false;
      }
    }
    return true;
  }

  protected void beforeRequest(@NotNull HttpServletRequest request) {
    logger.info("{} {}", request.getMethod(), request.getRequestURI());
  }
}
