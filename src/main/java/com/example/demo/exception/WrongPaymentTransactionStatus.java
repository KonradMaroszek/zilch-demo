package com.example.demo.exception;

import javax.validation.constraints.NotNull;

public class WrongPaymentTransactionStatus extends RuntimeException {
  public WrongPaymentTransactionStatus(@NotNull String message) {
    super(message);
  }
}
