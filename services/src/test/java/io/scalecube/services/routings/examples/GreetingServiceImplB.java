package io.scalecube.services.routings.examples;

import io.scalecube.services.examples.GreetingRequest;
import io.scalecube.services.examples.GreetingResponse;

import reactor.core.publisher.Mono;


public final class GreetingServiceImplB implements CanaryService {

  @Override
  public Mono<GreetingResponse> greeting(GreetingRequest name) {
    return Mono.just(new GreetingResponse("SERVICE_B_TALKING - hello to: " + name));
  }
}