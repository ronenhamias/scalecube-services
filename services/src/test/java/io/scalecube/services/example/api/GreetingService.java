package io.scalecube.services.example.api;

import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.example.BaseInterface;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public interface GreetingService extends BaseInterface{

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
  Flux<GreetingResponse> bidiGreeting(Publisher<GreetingRequest> request);

  @ServiceMethod
  Flux<GreetingResponse> bidiGreetingNotAuthorized(Flux<GreetingRequest> request);

  @ServiceMethod
  Flux<GreetingResponse> bidiGreetingIllegalArgumentException(Publisher<GreetingRequest> request);
}
