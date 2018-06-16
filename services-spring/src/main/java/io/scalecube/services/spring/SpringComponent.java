package io.scalecube.services.spring;

import org.springframework.stereotype.Component;

@Component
public class SpringComponent {

  public String ping() {
    return "SpringComponent";
  }
}
