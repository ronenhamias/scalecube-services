package io.scalecube.services.a.b.testing;

import io.scalecube.services.example.api.GreetingRequest;
import io.scalecube.services.example.api.GreetingResponse;

import reactor.core.publisher.Mono;


public final class GreetingServiceImplB implements CanaryService {

  @Override
  public Mono<GreetingResponse> greeting(GreetingRequest name) {
    return Mono.just(new GreetingResponse("SERVICE_B_TALKING - hello to: " + name));
  }
}
