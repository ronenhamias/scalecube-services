package io.scalecube.services.transport.dispatchers;

import io.scalecube.services.Reflect;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.transport.AbstractServiceMethodDispatcher;

import org.reactivestreams.Publisher;

import java.lang.reflect.Method;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MyLocalDispatcher extends AbstractServiceMethodDispatcher<ServiceMessage> {


  public MyLocalDispatcher(String qualifier, Object serviceObject, Method method) {
    super(qualifier, serviceObject, method);
  }

  @Override
  public Mono<Void> fireAndForget(ServiceMessage request) {
    try {
      return Mono.from(Reflect.invokeMessage(serviceObject, method, request));
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

  @Override
  public Mono<ServiceMessage> requestResponse(ServiceMessage request) {
    try {
      return Mono.from(Reflect.invokeMessage(serviceObject, method, request)).map(this::toReturnMessage);
    } catch (Throwable e) {
      return Mono.error(e);
    }
  }

  @Override
  public Flux<ServiceMessage> requestStream(ServiceMessage request) {
    try {
      return Flux.from(Reflect.invokeMessage(serviceObject, method, request)).map(this::toReturnMessage);
    } catch (Exception e) {
      return Flux.error(e);
    }
  }

  @Override
  public Flux<ServiceMessage> requestChannel(Publisher<ServiceMessage> publisher) {
    return Flux.from(publisher).map(message -> {
      try {
        return Reflect.invokeMessage(serviceObject, method, message);
      } catch (Exception e) {
        return Mono.error(e);
      }
    }).map(this::toReturnMessage);
  }
}
