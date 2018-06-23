package io.scalecube.services.routings.examples;

import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;
import io.scalecube.services.examples.GreetingRequest;
import io.scalecube.services.examples.GreetingResponse;

import reactor.core.publisher.Mono;


@Service
public interface CanaryService {

  @ServiceMethod
  Mono<GreetingResponse> greeting(GreetingRequest request);

}
