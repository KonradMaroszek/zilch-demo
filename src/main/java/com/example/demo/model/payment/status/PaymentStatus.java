package com.example.demo.model.payment.status;

import javax.validation.constraints.NotNull;

public enum PaymentStatus {
  FIRST_PART_PAYED("FIRST_PART_PAYED"),
  SECOND_PART_PAYED("SECOND_PART_PAYED"),
  THIRD_PART_PAYED("THIRD_PART_PAYED"),
  PAYED("PAYED");

  @NotNull private String code;

  PaymentStatus(@NotNull String code) {
    this.code = code;
  }

  public @NotNull String getCode() {
    return code;
  }
}
