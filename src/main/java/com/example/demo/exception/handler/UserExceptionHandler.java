package com.example.demo.exception.handler;

import static com.example.demo.constant.Constants.APP_ORIGIN;
import static com.example.demo.constant.Constants.X_TRACE_ID_HEADER;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.example.demo.exception.NotFoundException;
import com.example.demo.model.error.ErrorResponse;
import javax.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Order(1)
@ControllerAdvice
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserExceptionHandler {
  /** User not found exception handler. */
  @ResponseBody
  @ResponseStatus(NOT_FOUND)
  @ExceptionHandler(NotFoundException.class)
  public static ErrorResponse handleConstraintViolationException(
      NotFoundException error, HttpServletResponse response) {
    logger.error("NotFoundException: ", error);

    return ErrorResponse.builder()
        .code(NOT_FOUND.value())
        .message(error.getMessage())
        .traceId(response.getHeader(X_TRACE_ID_HEADER))
        .origin(APP_ORIGIN)
        .build();
  }
}
