package io.scalecube.services.transport;

import io.scalecube.services.Reflect;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.transport.api.ServiceMethodDispatcher;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

public abstract class AbstractServiceMethodDispatcher<REQ> implements ServiceMethodDispatcher<REQ> {

  protected final Method method;
  protected final Object serviceObject;
  protected final Class requestType;
  protected final String methodName;
  protected final String qualifier;
  protected String returnType;

  public AbstractServiceMethodDispatcher(String qualifier, Object serviceObject, Method method) {
    this.qualifier = qualifier;
    this.serviceObject = serviceObject;
    this.method = method;
    this.methodName = Reflect.methodName(method);
    this.requestType = Reflect.requestType(method);
    this.returnType = Reflect.parameterizedReturnType(method).getName();
  }

  protected ServiceMessage toReturnMessage(Object obj) {
    return obj instanceof ServiceMessage
        ? (ServiceMessage) obj
        : ServiceMessage.builder()
            .qualifier(qualifier)
            .header("_type", returnType)
            .data(obj)
            .build();
  }
}
