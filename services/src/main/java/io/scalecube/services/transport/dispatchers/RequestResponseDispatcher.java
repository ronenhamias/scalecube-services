package io.scalecube.services.transport.dispatchers;

import static io.scalecube.services.Reflect.invokeMessage;

import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.transport.AbstractServiceMethodDispatcher;

import org.reactivestreams.Publisher;

import java.lang.reflect.Method;

import reactor.core.publisher.Mono;

public class RequestResponseDispatcher
    extends AbstractServiceMethodDispatcher<ServiceMessage, Publisher<ServiceMessage>> {

  public RequestResponseDispatcher(String qualifier, Object serviceObject, Method method) {
    super(qualifier, serviceObject, method);
  }

  @Override
  public Publisher<ServiceMessage> invoke(ServiceMessage request) {
    try {
      return Mono.from(invokeMessage(serviceObject, method, request)).map(this::toReturnMessage);
    } catch (Throwable e) {
      return Mono.error(e);
    }
  }
}
