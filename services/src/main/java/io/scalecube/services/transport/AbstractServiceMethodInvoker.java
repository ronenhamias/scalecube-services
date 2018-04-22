package io.scalecube.services.transport;

import io.scalecube.services.Reflect;
import io.scalecube.services.ServiceMessageCodec;
import io.scalecube.services.api.ServiceMessage;

import java.lang.reflect.Method;

public abstract class AbstractServiceMethodInvoker<REQ, RESP> implements ServiceMethodInvoker<REQ> {

  protected final Method method;

  protected final Object serviceObject;

  protected final Class requestType;

  protected final ServiceMessageCodec<?> payloadCodec;

  private final String methodName;

  @Override
  public ServiceMessageCodec getCodec() {
    return this.payloadCodec;
  }

  public String methodName() {
    return this.methodName;
  }

  public AbstractServiceMethodInvoker(Object serviceObject,
      Method method,
      ServiceMessageCodec<?> payloadCodec) {

    this.serviceObject = serviceObject;
    this.method = method;
    this.methodName = Reflect.methodName(method);
    this.requestType = Reflect.requestType(method);
    this.payloadCodec = payloadCodec;
  }

  protected ServiceMessage toReturnMessage(Object obj) {
    return obj instanceof ServiceMessage ? (ServiceMessage) obj
        : ServiceMessage.builder().header("_type", Reflect.parameterizedReturnType(method).getName())
            .data(obj)
            .build();
  }
}
