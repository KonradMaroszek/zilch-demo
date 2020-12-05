package com.example.demo.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.validation.constraints.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@Configuration
public class AppConfig {
  @Bean
  @NotNull
  ObjectMapper getObjectMapper() {
    return new ObjectMapper();
  }
}
