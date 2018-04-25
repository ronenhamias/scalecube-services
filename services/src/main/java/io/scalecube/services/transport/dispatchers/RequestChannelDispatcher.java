package io.scalecube.services.transport.dispatchers;

import io.scalecube.services.Reflect;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.transport.AbstractServiceMethodDispatcher;

import org.reactivestreams.Publisher;

import java.lang.reflect.Method;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RequestChannelDispatcher
    extends AbstractServiceMethodDispatcher<Publisher<ServiceMessage>, Publisher<ServiceMessage>> {

  public RequestChannelDispatcher(String qualifier, Object serviceObject, Method method) {
    super(qualifier, serviceObject, method);
  }

  @Override
  public Publisher<ServiceMessage> invoke(Publisher<ServiceMessage> publisher) {
    return Flux.from(publisher).map(message -> {
      try {
        return Reflect.invokeMessage(serviceObject, method, message);
      } catch (Exception e) {
        return Mono.error(e);
      }
    }).map(this::toReturnMessage);
  }
}
