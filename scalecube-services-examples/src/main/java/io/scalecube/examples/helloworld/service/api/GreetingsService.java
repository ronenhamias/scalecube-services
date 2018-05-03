package io.scalecube.examples.helloworld.service.api;

import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;

import reactor.core.publisher.Mono;

@Service("io.scalecube.Greetings")
public interface GreetingsService {

  @ServiceMethod("sayHello")
  Mono<Greeting> sayHello(String name);
  
}
