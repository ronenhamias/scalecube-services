package io.scalecube.services;

import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;
import io.scalecube.services.api.ServiceMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public interface GreetingService {

  @ServiceMethod
  void notifyGreeting();

  @ServiceMethod
  Mono<String> greetingNoParams();

  @ServiceMethod
  Mono<String> greeting(String string);

  @ServiceMethod
  Mono<GreetingResponse> greetingPojo(GreetingRequest name);

  @ServiceMethod
  Mono<GreetingResponse> greetingNotAuthorized(GreetingRequest name);

  @ServiceMethod
  Mono<GreetingResponse> greetingRequestTimeout(GreetingRequest request);

  @ServiceMethod
  Mono<GreetingResponse> greetingRequest(GreetingRequest string);

  @ServiceMethod
  Mono<ServiceMessage> greetingMessage(ServiceMessage request);

  @ServiceMethod
  Mono<Void> greetingVoid(GreetingRequest request);

  @ServiceMethod
  Mono<Void> failingVoid(GreetingRequest request);

  @ServiceMethod
  Mono<Void> throwingVoid(GreetingRequest request);

  @ServiceMethod
  Mono<GreetingResponse> failingRequest(GreetingRequest request);

  @ServiceMethod
  Mono<GreetingResponse> exceptionRequest(GreetingRequest request);

  @ServiceMethod
  Flux<GreetingResponse> bidiGreeting(Flux<GreetingRequest> request);

  @ServiceMethod
  Flux<GreetingResponse> bidiGreetingNotAuthorized(Flux<GreetingRequest> request);

  @ServiceMethod
  Flux<GreetingResponse> bidiGreetingIllegalArgumentException(Flux<GreetingRequest> request);
}
