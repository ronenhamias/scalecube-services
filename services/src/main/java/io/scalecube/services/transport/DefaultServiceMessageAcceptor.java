package io.scalecube.services.transport;

import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.api.ServiceMessageHandler;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;

public final class DefaultServiceMessageAcceptor implements ServiceMessageHandler {

  private final LocalServiceHandlers serviceHandlers;

  public DefaultServiceMessageAcceptor(LocalServiceHandlers serviceHandlers) {
    this.serviceHandlers = serviceHandlers;
  }

  @Override
  public Publisher<ServiceMessage> invoke(Publisher<ServiceMessage> publisher) {
    Flux<ServiceMessage> messages = Flux.from(publisher).share();
    return messages.take(1).flatMap(message -> {
      ServiceMessageHandler dispatcher = serviceHandlers.get(message.qualifier());
      return dispatcher.invoke(messages.startWith(message));
    });
  }
}
