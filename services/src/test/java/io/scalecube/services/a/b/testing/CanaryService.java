package io.scalecube.services.a.b.testing;

import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;
import io.scalecube.services.example.api.GreetingRequest;
import io.scalecube.services.example.api.GreetingResponse;

import reactor.core.publisher.Mono;


@Service
public interface CanaryService {

  @ServiceMethod
  Mono<GreetingResponse> greeting(GreetingRequest request);

}
