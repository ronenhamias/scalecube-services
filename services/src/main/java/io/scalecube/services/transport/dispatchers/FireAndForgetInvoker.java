package io.scalecube.services.transport.dispatchers;

import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.codecs.api.ServiceMessageDataCodec;
import io.scalecube.services.transport.AbstractServiceMethodDispatcher;

import org.reactivestreams.Publisher;

import java.lang.reflect.Method;

import reactor.core.publisher.Mono;

public class FireAndForgetInvoker
    extends AbstractServiceMethodDispatcher<ServiceMessage, Publisher<Void>> {

  public FireAndForgetInvoker(String qualifier,
      Object serviceObject,
      Method method,
      ServiceMessageDataCodec payloadCodec) {
    super(qualifier, serviceObject, method, payloadCodec);
  }

  @Override
  public Publisher<ServiceMessage> invoke(ServiceMessage request) {
    ServiceMessage message = payloadCodec.decodeData(request, super.requestType);
    try {
      return Mono.from(super.dispatchServiceMethod(message)).map(this::toReturnMessage);
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

}
