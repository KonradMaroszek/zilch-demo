package com.example.demo.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableRetry
@EnableWebMvc
@Configuration
public class AppConfig {
  @Bean
  @NotNull
  ObjectMapper getObjectMapper() {
    return new ObjectMapper();
  }
}
