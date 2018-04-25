package io.scalecube.services.transport.api;

import io.scalecube.services.api.ServiceMessage;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ServiceMethodDispatcher<REQ> {
  Mono<Void> fireAndForget(REQ payload);
  Mono<ServiceMessage> requestResponse(REQ payload);
  Flux<ServiceMessage> requestStream(REQ payload);
  Flux<ServiceMessage> requestChannel(Publisher<REQ> payloads);
}
