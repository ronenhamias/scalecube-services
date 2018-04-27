package io.scalecube.services.transport;

import io.scalecube.services.Reflect;
import io.scalecube.services.codecs.api.ServiceMessageCodec;
import io.scalecube.services.transport.api.CommunicationMode;
import io.scalecube.services.transport.api.ServiceMethodDispatcher;
import io.scalecube.services.transport.dispatchers.FireAndForgetInvoker;
import io.scalecube.services.transport.dispatchers.RequestChannelDispatcher;
import io.scalecube.services.transport.dispatchers.RequestResponseDispatcher;
import io.scalecube.services.transport.dispatchers.RequestStreamDispatcher;
import io.scalecube.services.transport.server.api.ServerTransport;
import io.scalecube.transport.Address;
import io.scalecube.transport.Addressing;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LocalServiceDispatchers {

  @SuppressWarnings("rawtypes")
  private final ConcurrentMap<String, ServiceMethodDispatcher> localServices = new ConcurrentHashMap<>();

  private List<Object> services;

  private Address serviceAddress;

  private ServerTransport server;

  private Map<String, ? extends ServiceMessageCodec> codecs;

  public Address serviceAddress() {
    return this.serviceAddress;
  }
  
  private LocalServiceDispatchers() {
    // noop. use create().
  }

  public static class Builder {
    private Object[] services;
    private ServerTransport server;
    private Address serviceAddress;
    public Map<String, ? extends ServiceMessageCodec> codecs;
    
    public Builder services(Object[] services) {
      this.services = services;
      return this;
    }

    public LocalServiceDispatchers build() {
      LocalServiceDispatchers dispatchers = new LocalServiceDispatchers(this);
   
      return dispatchers;
    }

    public Builder server(ServerTransport server) {
      this.server = server;
      return this;
    }
    
    public Builder codecs(Map<String, ? extends ServiceMessageCodec> codecs) {
      this.codecs = codecs;
      return this;
    }
  }

  public static LocalServiceDispatchers.Builder builder() {
    return new Builder();
  }

  public Address start() {
    if (services != null && !services.isEmpty()) {
      this.server.accept(new DefaultServerMessageAcceptor(this, this.codecs));
      InetSocketAddress inet = this.server.bindAwait(new InetSocketAddress(Addressing.getLocalIpAddress(), 0));
      this.serviceAddress = Address.create(inet.getHostString(), inet.getPort());
    } else {
      this.serviceAddress = Address.from("localhost:0");
    }
    return serviceAddress;
  }
  private LocalServiceDispatchers(Builder builder) {
    this.services = Arrays.asList(builder.services);
    this.codecs = builder.codecs;
    this.server = builder.server;
    
    this.services().forEach(service -> {
      Reflect.serviceInterfaces(service).forEach(serviceInterface -> {

        Reflect.serviceMethods(serviceInterface).forEach((key, method) -> {
          Optional<CommunicationMode> communicationMode = CommunicationMode.of(method);
          String qualifier = Reflect.qualifier(serviceInterface, method);
          if (communicationMode.get().equals(CommunicationMode.REQUEST_ONE)) {
            this.register(qualifier, new RequestResponseDispatcher(qualifier, service, method));

          } else if (communicationMode.get().equals(CommunicationMode.REQUEST_STREAM)) {
            this.register(qualifier, new RequestChannelDispatcher(qualifier, service, method));

          } else if (communicationMode.get().equals(CommunicationMode.ONE_WAY)) {
            this.register(qualifier, new FireAndForgetInvoker(qualifier, service, method));

          } else if (communicationMode.get().equals(CommunicationMode.REQUEST_MANY)) {
            this.register(qualifier, new RequestStreamDispatcher(qualifier, service, method));
          }
        });
      });
    });
  }

  public boolean contains(String qualifier) {
    return localServices.get(qualifier) != null;
  }

  public Collection<Object> services() {
    return Collections.unmodifiableCollection(this.services);
  }

  public ServiceMethodDispatcher getDispatcher(String qualifier) {
    return localServices.get(qualifier);
  }

  private void register(final String qualifier, ServiceMethodDispatcher handler) {
    localServices.put(qualifier, handler);
  }

  

}
