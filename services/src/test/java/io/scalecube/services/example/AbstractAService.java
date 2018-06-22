package io.scalecube.services.example;

import reactor.core.publisher.Mono;

public class AbstractAService {

  public Mono<String> helloFromBase(String name) {
    return Mono.just("Base class greetings to: " + name);
  }

  
}
