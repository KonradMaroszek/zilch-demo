package com.example.demo.exception;

import javax.validation.constraints.NotNull;

public class TransactionNotFoundException extends NotFoundException {
  public TransactionNotFoundException(@NotNull String message) {
    super(message);
  }
}
