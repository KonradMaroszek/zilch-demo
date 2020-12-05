package com.example.demo.exception.handler;

import static com.example.demo.constant.Constants.APP_ORIGIN;
import static com.example.demo.constant.Constants.X_TRACE_ID_HEADER;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.example.demo.model.error.ErrorResponse;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@ControllerAdvice
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalDefaultExceptionHandler {
  /** Default exception handler. */
  @ResponseBody
  @ResponseStatus(INTERNAL_SERVER_ERROR)
  @ExceptionHandler(value = Exception.class)
  public ErrorResponse defaultErrorHandler(Exception error, HttpServletResponse response) {
    logger.error("An unexpected failure occurred. ", error);

    return ErrorResponse.builder()
        .code(INTERNAL_SERVER_ERROR.value())
        .message("Unexpected error. ")
        .traceId(response.getHeader(X_TRACE_ID_HEADER))
        .origin(APP_ORIGIN)
        .build();
  }

  /** Contraint violation exception handler. */
  @ResponseBody
  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException.class)
  public static ErrorResponse handleConstraintViolationException(
      ConstraintViolationException error, HttpServletResponse response) {
    logger.error("ConstraintViolationException: ", error);

    return ErrorResponse.builder()
        .code(BAD_REQUEST.value())
        .message("Call not valid due to validation error: " + error.getMessage())
        .traceId(response.getHeader(X_TRACE_ID_HEADER))
        .origin(APP_ORIGIN)
        .build();
  }

  /** Missing request header exception handler. */
  @ResponseBody
  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({MissingRequestHeaderException.class, MissingPathVariableException.class})
  public static ErrorResponse handleMissingRequestHeaderException(
      Exception error, HttpServletResponse response) {
    logger.error("MissingRequestHeaderException: ", error);

    return ErrorResponse.builder()
        .code(BAD_REQUEST.value())
        .message("Unexpected error. Missing header. " + error.getMessage())
        .traceId(response.getHeader(X_TRACE_ID_HEADER))
        .origin(APP_ORIGIN)
        .build();
  }

  /** Type mismatch exception handler. */
  @ResponseBody
  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({MethodArgumentTypeMismatchException.class})
  public static ErrorResponse handleMissingParameterTypeException(
      MethodArgumentTypeMismatchException error, HttpServletResponse response) {
    logger.error("MethodArgumentTypeMismatchException: ", error);

    return ErrorResponse.builder()
        .code(BAD_REQUEST.value())
        .message("Unexpected error. Argument type mismatch. " + error.getMessage())
        .traceId(response.getHeader(X_TRACE_ID_HEADER))
        .origin(APP_ORIGIN)
        .build();
  }

  /** No handler found exception handler. */
  @ResponseBody
  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler(NoHandlerFoundException.class)
  public ErrorResponse defaultNoHandlerFoundErrorHandler(
      NoHandlerFoundException error, HttpServletResponse response) {
    logger.error("NoHandlerFoundException: ", error);

    return ErrorResponse.builder()
        .code(BAD_REQUEST.value())
        .message("Unexpected error. " + error.getMessage())
        .traceId(response.getHeader(X_TRACE_ID_HEADER))
        .origin(APP_ORIGIN)
        .build();
  }
}
