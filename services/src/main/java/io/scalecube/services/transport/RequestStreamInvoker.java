package io.scalecube.services.transport;

import io.scalecube.services.Reflect;
import io.scalecube.services.ServiceMessageCodec;
import io.scalecube.services.api.ServiceMessage;

import org.reactivestreams.Publisher;

import java.lang.reflect.Method;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RequestStreamInvoker extends AbstractServiceMethodInvoker<ServiceMessage, Publisher<ServiceMessage>> {

  public RequestStreamInvoker(Object serviceObject, Method method, 
      ServiceMessageCodec<?> payloadCodec) {
    
    super(serviceObject, method, payloadCodec);
  }

  public Publisher<ServiceMessage> invoke(ServiceMessage request) {
    
    ServiceMessage message = payloadCodec.decodeData(request, super.requestType);
    try {
      return Flux.from(Reflect.invokeMessage(serviceObject, method, message))
          .map(obj->toReturnMessage(obj));
      
    } catch (Exception e) {
      return Mono.error(e);
    }
    
  }

}
