package io.scalecube.services.transport.dispatchers;

import io.scalecube.services.Reflect;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.transport.AbstractServiceMethodDispatcher;

import org.reactivestreams.Publisher;

import java.lang.reflect.Method;

import reactor.core.publisher.Mono;

public class FireAndForgetInvoker
    extends AbstractServiceMethodDispatcher<ServiceMessage, Publisher<Void>> {

  public FireAndForgetInvoker(String qualifier, Object serviceObject, Method method) {
    super(qualifier, serviceObject, method);
  }

  @Override
  public Publisher<ServiceMessage> invoke(ServiceMessage request) {
    try {
      return Mono.from(Reflect.invokeMessage(serviceObject, method, request)).map(this::toReturnMessage);
    } catch (Exception e) {
      return Mono.error(e);
    }
  }
}
