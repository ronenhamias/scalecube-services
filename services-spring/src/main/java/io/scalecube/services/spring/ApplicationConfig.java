package io.scalecube.services.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

  @Bean
  public SomeComponent someComponent() {
    return new SomeComponent();
  }

  @Bean
  public SpringComponent springComponent() {
    return new SpringComponent();
  }

  @Bean
  public SpringService springService() {
    return new SpringServiceImpl();
  }
}
