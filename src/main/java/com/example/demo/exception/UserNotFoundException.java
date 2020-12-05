package com.example.demo.exception;

import javax.validation.constraints.NotNull;

public class UserNotFoundException extends NotFoundException {
  public UserNotFoundException(@NotNull String message) {
    super(message);
  }
}
