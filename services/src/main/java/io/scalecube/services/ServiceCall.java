package io.scalecube.services;

import io.scalecube.services.api.ErrorData;
import io.scalecube.services.api.NullData;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.api.ServiceMessageHandler;
import io.scalecube.services.codec.ServiceMessageDataCodec;
import io.scalecube.services.exceptions.ExceptionProcessor;
import io.scalecube.services.exceptions.ServiceUnavailableException;
import io.scalecube.services.metrics.Metrics;
import io.scalecube.services.registry.api.ServiceRegistry;
import io.scalecube.services.routing.Router;
import io.scalecube.services.transport.LocalServiceHandlers;
import io.scalecube.services.transport.client.api.ClientTransport;
import io.scalecube.transport.Address;

import com.codahale.metrics.Timer;
import com.google.common.reflect.Reflection;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class ServiceCall {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCall.class);

  private final ClientTransport transport;
  private final LocalServiceHandlers serviceHandlers;
  private final ServiceRegistry serviceRegistry;

  public ServiceCall(ClientTransport transport,
      LocalServiceHandlers serviceHandlers,
      ServiceRegistry serviceRegistry) {
    this.transport = transport;
    this.serviceHandlers = serviceHandlers;
    this.serviceRegistry = serviceRegistry;
  }

  public Call call() {
    return new Call(this.transport, this.serviceHandlers, this.serviceRegistry);
  }

  public static class Call {

    private Router router;
    private Metrics metrics;
    private Timer latency;
    private ClientTransport transport;
    private ServiceMessageDataCodec dataCodec;
    private LocalServiceHandlers serviceHandlers;
    private final ServiceRegistry serviceRegistry;

    public Call(ClientTransport transport,
        LocalServiceHandlers serviceHandlers,
        ServiceRegistry serviceRegistry) {
      this.transport = transport;
      this.serviceRegistry = serviceRegistry;
      this.dataCodec = new ServiceMessageDataCodec();
      this.serviceHandlers = serviceHandlers;
    }

    public Call router(Router router) {
      this.router = router;
      return this;
    }

    public Call metrics(Metrics metrics) {
      this.metrics = metrics;
      this.latency = Metrics.timer(this.metrics, ServiceCall.class.getName(), "invoke");
      return this;
    }

    /**
     * Issues fire-and-rorget request.
     *
     * @param request request to send.
     * @return Mono of type Void.
     */
    public Mono<Void> oneWay(ServiceMessage request) {
      return requestOne(request).map(message -> null);
    }

    /**
     * Invoke a request message and invoke a service by a given service name and method name. expected headers in
     * request: ServiceHeaders.SERVICE_REQUEST the logical name of the service. ServiceHeaders.METHOD the method name to
     * invoke message uses the router to select the target endpoint service instance in the cluster. Throws Exception in
     * case of an error or TimeoutException if no response if a given duration.
     *
     * @param request request with given headers.
     * @return {@link Publisher} with service call dispatching result.
     */
    public Mono<ServiceMessage> requestOne(ServiceMessage request) {
      return requestOne(request, request.responseType() != null ? request.responseType() : Object.class);
    }

    /**
     * Invoke a request message and invoke a service by a given service name and method name. expected headers in
     * request: ServiceHeaders.SERVICE_REQUEST the logical name of the service. ServiceHeaders.METHOD the method name to
     * invoke message uses the router to select the target endpoint service instance in the cluster. Throws Exception in
     * case of an error or TimeoutException if no response if a given duration.
     *
     * @param request request with given headers.
     * @param returnType return type of service message response.
     * @return {@link Publisher} with service call dispatching result.
     */
    public Mono<ServiceMessage> requestOne(ServiceMessage request, final Class<?> returnType) {
      return requestMany(request, returnType).as(Mono::from);
    }
    
    /**
     * Issues request to service which returns stream of service messages back.
     *
     * @param request request with given headers.
     * @return {@link Publisher} with service call dispatching result.
     */
    public Flux<ServiceMessage> requestMany(ServiceMessage request) {
      final Class<?> returnType = request.responseType() != null ? request.responseType() : Object.class;
      return requestMany(request, returnType);

    }

    /**
     * Issues request to service which returns stream of service messages back.
     *
     * @param request request with given headers.
     * @return {@link Publisher} with service call dispatching result.
     */
    public Flux<ServiceMessage> requestMany(ServiceMessage request, Class<?> returnType) {
      Messages.validate().serviceRequest(request);
      String qualifier = request.qualifier();
      if (serviceHandlers.contains(qualifier)) {
        return Flux.from(serviceHandlers.get(qualifier).invoke(Mono.just(request)))
            .onErrorMap(ExceptionProcessor::mapException);
      } else {
        ServiceReference serviceReference =
            router.route(serviceRegistry, request).orElseThrow(() -> noReachableMemberException(request));
        
        Address address =
            Address.create(serviceReference.host(), serviceReference.port());
        
        return transport.create(address)
            .requestBidirectional(Flux.just(request))
            .map(message -> {
              if (ExceptionProcessor.isError(message)) {
                throw ExceptionProcessor.toException(dataCodec.decode(message, ErrorData.class));
              } else {
                return dataCodec.decode(message, returnType);
              }
            });
      }
    }
    
    
    /**
     * Issues request to service which returns stream of service messages back.
     *
     * @param request request with given headers.
     * @return {@link Publisher} with service call dispatching result.
     */
    public Flux<ServiceMessage> requestChannel(Flux<ServiceMessage> request, Class<?> returnType) {
      
     
      Tuple2<Flux<ServiceMessage>, Flux<ServiceMessage>> headAndTail = Tuples.of(request.take(1), request);
      CountDownLatch latch = new CountDownLatch(1);
      
      AtomicReference<String> qualifier = new AtomicReference<>();
      //one of them should not be null.
      AtomicReference<Address> address = new AtomicReference<>(); 
      AtomicReference<ServiceMessageHandler> serviceMessageHandler = new AtomicReference<>();
      
      headAndTail.getT1().subscribe(head -> {
        Messages.validate().serviceRequest(head);
        qualifier.set(head.qualifier());
        if (serviceHandlers.contains(qualifier.get())) {
          serviceMessageHandler.set(serviceHandlers.get(qualifier.get()));
        } else {
          ServiceReference serviceReference =
              router.route(serviceRegistry, head).orElseThrow(() -> noReachableMemberException(head));
          address.set(Address.create(serviceReference.host(), serviceReference.port()));
        }
        latch.countDown();
      });
      
      return headAndTail.getT2().transform(nextMessage -> {
        try {
          latch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException ignoredException) {
          return Flux.error(ignoredException);
        }
        if (address.get() != null) {
          return transport.create(address.get())
              .requestBidirectional(request)
              .map(message -> {
                if (ExceptionProcessor.isError(message)) {
                  throw ExceptionProcessor.toException(dataCodec.decode(message, ErrorData.class));
                } else {
                  return dataCodec.decode(message, returnType);
                }
              });
        } else if (serviceMessageHandler.get() != null) {
          return Flux.from(serviceMessageHandler.get().invoke(request))
              .onErrorMap(ExceptionProcessor::mapException);
        } else {
          return Flux.error(new TimeoutException("No reachable member with such service: " + qualifier.get()));
        }
      });
           
      
    }
    
    
    
    /**
     * Create proxy creates a java generic proxy instance by a given service interface.
     *
     * @param serviceInterface Service Interface type.
     * @return newly created service proxy object.
     */
    public <T> T api(Class<T> serviceInterface) {

      final Call serviceCall = this;

      return Reflection.newProxy(serviceInterface, (proxy, method, args) -> {

        Object check = objectToStringEqualsHashCode(method.getName(), serviceInterface, args);
        if (check != null) {
          return check; // toString, hashCode was invoked.
        }

        Metrics.mark(serviceInterface, metrics, method, "request");
        Class<?> parameterizedReturnType = Reflect.parameterizedReturnType(method);
        CommunicationMode mode = Reflect.communicationMode(method);

        ServiceMessage request = ServiceMessage.builder()
            .qualifier(Reflect.serviceName(serviceInterface), method.getName())
            .data(method.getParameterCount() != 0 ? args[0] : NullData.NULL_DATA)
            .build();

        switch (mode) {
          case FIRE_AND_FORGET:
            return serviceCall.oneWay(request);
          case REQUEST_RESPONSE:
            return serviceCall.requestOne(request, parameterizedReturnType)
                .transform(mono -> parameterizedReturnType.equals(ServiceMessage.class) ? mono
                    : mono.map(ServiceMessage::data));
          case REQUEST_STREAM:
            return serviceCall.requestMany(request)
                .transform(flux -> parameterizedReturnType.equals(ServiceMessage.class) ? flux
                    : flux.map(ServiceMessage::data));
          case REQUEST_CHANNEL:
            // falls to default
          default:
            throw new IllegalArgumentException("Communication mode is not supported: " + method);
        }
      });
    }

    private static ServiceUnavailableException noReachableMemberException(ServiceMessage request) {
      LOGGER.error("Failed  to invoke service, No reachable member with such service definition [{}], args [{}]",
          request.qualifier(), request);
      return new ServiceUnavailableException("No reachable member with such service: " + request.qualifier());
    }

    private static Object objectToStringEqualsHashCode(String method, Class<?> serviceInterface, Object... args) {
      if (method.equals("hashCode")) {
        return serviceInterface.hashCode();
      } else if (method.equals("equals")) {
        return serviceInterface.equals(args[0]);
      } else if (method.equals("toString")) {
        return serviceInterface.toString();
      } else {
        return null;
      }
    }
  }
}
