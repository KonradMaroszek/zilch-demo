package com.example.demo.constant;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class Constants {
  public static final String X_TRACE_ID_PARAMETER = "x-trace-id";
  public static final String X_TRACE_ID_HEADER = "x-trace-id";
  public static final String MDC_TRACE_ID = "x-trace-id";
  public static final String APP_ORIGIN = "DEMO_APP";
}
