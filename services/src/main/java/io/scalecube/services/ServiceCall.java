package io.scalecube.services;

import io.scalecube.services.api.NullData;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.api.ServiceMessageHandler;
import io.scalecube.services.exceptions.ExceptionProcessor;
import io.scalecube.services.exceptions.ServiceUnavailableException;
import io.scalecube.services.metrics.Metrics;
import io.scalecube.services.registry.api.ServiceRegistry;
import io.scalecube.services.routing.Router;
import io.scalecube.services.routing.Routers;
import io.scalecube.services.transport.HeadAndTail;
import io.scalecube.services.transport.LocalServiceHandlers;
import io.scalecube.services.transport.client.api.ClientTransport;
import io.scalecube.transport.Address;

import com.codahale.metrics.Timer;
import com.google.common.reflect.Reflection;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

import static io.scalecube.services.CommunicationMode.REQUEST_CHANNEL;

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
    private ResponseMapper responseMapper = DefaultResponseMapper.DEFAULT_INSTANCE;

    private final ClientTransport transport;
    private final LocalServiceHandlers serviceHandlers;
    private final ServiceRegistry serviceRegistry;
    private BiFunction<Class<T>, Method, ServiceMessage> callToMessage = (tClass, method1) -> ServiceMessage.builder()
            .qualifier(Reflect.serviceName(tClass), method1.getName())
            .data(method1.getParameterCount() != 0 ? args[0] : NullData.NULL_DATA)
            .build();;

    public Call(ClientTransport transport, LocalServiceHandlers serviceHandlers, ServiceRegistry serviceRegistry) {
      this.transport = transport;
      this.serviceRegistry = serviceRegistry;
      this.serviceHandlers = serviceHandlers;
    }

    public Call router(Class<? extends Router> routerType) {
      this.router = Routers.getRouter(routerType);
      return this;
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

    public Call responseMapper(ResponseMapper responseMapper) {
      this.responseMapper = responseMapper;
      return this;
    }

    /**
     * Issues fire-and-rorget request.
     *
     * @param request request message to send.
     * @return mono publisher completing normally or with error.
     */
    public Mono<Void> fireAndForget(ServiceMessage request) {
      return requestResponse(request).map(message -> null);
    }

    /**
     * Issues request-and-reply request.
     *
     * @param request request message to send.
     * @return mono publisher completing with single response message or with error.
     */
    public Mono<ServiceMessage> requestResponse(ServiceMessage request) {
      return requestBidirectional(Mono.just(request)).as(Mono::from);
    }

    /**
     * Issues request-and-reply request.
     *
     * @param request request message to send.
     * @return mono publisher completing with single response message or with error.
     */
    public Mono<ServiceMessage> requestResponse(ServiceMessage request, Class<?> returnType) {
      return requestBidirectional(Mono.just(request), returnType).as(Mono::from);
    }

    /**
     * Issues request to service which returns stream of service messages back.
     *
     * @param request request with given headers.
     * @return {@link Publisher} with service call dispatching result.
     */
    public Flux<ServiceMessage> requestStream(ServiceMessage request) {
      return requestBidirectional(Mono.just(request));
    }

    public Flux<ServiceMessage> requestBidirectional(Publisher<ServiceMessage> publisher) {
      return requestBidirectional(publisher, null);
    }

    public Flux<ServiceMessage> requestBidirectional(Publisher<ServiceMessage> publisher, Class<?> returnType) {
      return Flux.from(HeadAndTail.createFrom(publisher)).flatMap(pair -> {

        ServiceMessage request = pair.head();
        Flux<ServiceMessage> requestPublisher = Flux.from(pair.tail()).startWith(request);

        Messages.validate().serviceRequest(request);
        String qualifier = request.qualifier();

        if (serviceHandlers.contains(qualifier)) {
          ServiceMessageHandler serviceHandler = serviceHandlers.get(qualifier);
          return serviceHandler.invoke(requestPublisher).onErrorMap(ExceptionProcessor::mapException);
        } else {

          ServiceReference serviceReference =
              router.route(serviceRegistry, request)
                  .orElseThrow(() -> noReachableMemberException(request));

          Address address =
              Address.create(serviceReference.host(), serviceReference.port());

          Flux<ServiceMessage> responsePublisher =
              transport.create(address).requestBidirectional(requestPublisher);

          if (responseMapper == null) {
            return responsePublisher;
          } else {
            Class<?> responseType;
            if (returnType != null) {
              responseType = returnType;
            } else {
              responseType = Object.class;
            }
            return responsePublisher.map(response -> responseMapper.apply(response, responseType));
          }
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

        Flux<ServiceMessage> pub = Flux.empty();

        ServiceMessage request = ServiceMessage.builder()
            .qualifier(Reflect.serviceName(serviceInterface), method.getName())
            .data(method.getParameterCount() != 0 ? args[0] : NullData.NULL_DATA)
            .build();

        switch (mode) {
          case FIRE_AND_FORGET:
            return serviceCall.fireAndForget(request);
          case REQUEST_RESPONSE:
            return serviceCall.requestResponse(request, parameterizedReturnType)
                .transform(mono -> parameterizedReturnType.equals(ServiceMessage.class) ? mono
                    : mono.map(ServiceMessage::data));
          case REQUEST_STREAM:
            return serviceCall.requestStream(request)
                .transform(flux -> parameterizedReturnType.equals(ServiceMessage.class) ? flux
                    : flux.map(ServiceMessage::data));
          case REQUEST_CHANNEL:
            return serviceCall.requestBidirectional(request);
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
