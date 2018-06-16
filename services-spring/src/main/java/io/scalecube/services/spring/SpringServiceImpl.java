package io.scalecube.services.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class SpringServiceImpl implements SpringService {

  @Autowired
  private SpringComponent springComponent;
  @Autowired
  private SomeComponent someComponent;

  @Override
  public Mono<String> invoke() {
    try {
      System.err.println(springComponent.ping());
      return someComponent.ping();
    } catch (Exception e) {
      return Mono.error(e);
    }
  }
}
