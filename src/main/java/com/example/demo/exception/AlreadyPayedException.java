package com.example.demo.exception;

import javax.validation.constraints.NotNull;

public class AlreadyPayedException extends RuntimeException {
  public AlreadyPayedException(@NotNull String message) {
    super(message);
  }
}
