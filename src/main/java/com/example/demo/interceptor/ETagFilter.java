package com.example.demo.interceptor;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ETagFilter extends OncePerRequestFilter {
  private Collection<Pattern> patterns;
  private Filter filter;

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
    if (applicable(uri)) {
      filter.doFilter(request, response, filterChain);
    } else {
      filterChain.doFilter(request, response);
    }
  }

  private boolean applicable(String uri) {
    if (patterns == null || patterns.isEmpty()) {
      return true;
    }

    for (var pattern : patterns) {
      if (pattern.matcher(uri).matches()) {
        return true;
      }
    }
    return false;
  }
}
