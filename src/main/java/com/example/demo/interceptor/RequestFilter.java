package com.example.demo.interceptor;

import static com.example.demo.constant.Constants.MDC_TRACE_ID;
import static com.example.demo.constant.Constants.X_TRACE_ID_HEADER;
import static com.example.demo.constant.Constants.X_TRACE_ID_PARAMETER;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE + 2)
public class RequestFilter extends OncePerRequestFilter {

  private static final Random random = new Random();

  private static @NotNull String generateNewTraceId() {
    StringBuilder randomString = new StringBuilder();

    for (int i = 0; i < 5; ++i) {
      randomString.append(random.nextInt(10));
    }

    String result = "DEMOAPP-" + new Date().getTime() + "-" + randomString + "-GB";
    logger.debug("Generated new traceId ({}) for request.", result);
    return result;
  }

  @Override
  public void doFilterInternal(
      @NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain chain)
      throws IOException, ServletException {

    logger.debug("doFilter triggered");
    try {
      addXTraceId(request, response);
      chain.doFilter(request, response);
    } finally {
      logger.debug("Clear MDC.");
      MDC.clear();
    }
  }

  private void addXTraceId(
      @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
    String traceIdRequestParameter =
        Optional.ofNullable(request.getParameter(X_TRACE_ID_PARAMETER)).orElse(null);
    String traceIdHeader = Optional.ofNullable(request.getHeader(X_TRACE_ID_HEADER)).orElse(null);
    String traceIdMdc = Optional.ofNullable(MDC.get(MDC_TRACE_ID)).orElse(null);

    String traceId =
        Stream.of(traceIdRequestParameter, traceIdHeader, traceIdMdc)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(RequestFilter::generateNewTraceId);

    MDC.put(MDC_TRACE_ID, traceId);
    response.setHeader(X_TRACE_ID_HEADER, traceId);
  }
}
