package com.example.demo.model.error;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
  @Positive private int code;
  @NotNull private String message;
  @NotBlank private String traceId;
  @NotBlank private String origin;
}
