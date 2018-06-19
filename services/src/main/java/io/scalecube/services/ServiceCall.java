package io.scalecube.services;

import static io.scalecube.services.CommunicationMode.REQUEST_CHANNEL;
import static java.util.Objects.requireNonNull;

import io.scalecube.services.api.NullData;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.codec.ServiceMessageDataCodec;
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

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class ServiceCall {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCall.class);

  private final ClientTransport transport;
  private final LocalServiceHandlers serviceHandlers;
  private final ServiceRegistry serviceRegistry;
  private final Router router;
  private final Metrics metrics;
  private final ServiceMessageDataCodec dataCodec = new ServiceMessageDataCodec();

  ServiceCall(Call call) {
    this.transport = call.transport;
    this.serviceHandlers = call.serviceHandlers;
    this.serviceRegistry = call.serviceRegistry;
    this.router = call.router;
    this.metrics = call.metrics;
  }

  public static class Call {

    private Router router;
    private Metrics metrics;

    private final ClientTransport transport;
    private final LocalServiceHandlers serviceHandlers;
    private final ServiceRegistry serviceRegistry;


    public Call(ClientTransport transport,
        LocalServiceHandlers serviceHandlers,
        ServiceRegistry serviceRegistry) {
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
      return this;
    }

    public ServiceCall create() {
      return new ServiceCall(this);
    }
  }

  /**
   * Issues fire-and-rorget request.
   *
   * @param request request message to send.
   * @return mono publisher completing normally or with error.
   */
  public Mono<Void> oneWay(ServiceMessage request) {
    return requestOne(request, Void.class).then();
  }

  /**
   * Issues request-and-reply request.
   *
   * @param request request message to send.
   * @return mono publisher completing with single response message or with error.
   */
  public Mono<ServiceMessage> requestOne(ServiceMessage request) {
    return requestOne(request, null);
  }

  /**
   * Issues request-and-reply request.
   *
   * @param request request message to send.
   * @param responseType type of response.
   * @return mono publisher completing with single response message or with error.
   */
  public Mono<ServiceMessage> requestOne(ServiceMessage request, Class<?> responseType) {
    String qualifier = request.qualifier();
    if (serviceHandlers.contains(qualifier)) { // local service.
      return serviceHandlers.requestResponse(request)
          .onErrorMap(ExceptionProcessor::mapException);
    } else { // remote service.
      return requestOne(request, responseType, addressLookup(request));
    }
  }

  /**
   * Given an address issues request-and-reply request to a remote address.
   *
   * @param request request message to send.
   * @param responseType type of response.
   * @param address of remote target service to invoke.
   * @return mono publisher completing with single response message or with error.
   */
  public Mono<ServiceMessage> requestOne(ServiceMessage request, Class<?> responseType, Address address) {
    requireNonNull(address, "requestOne address paramter is required and must not be null");
    return transport.create(address)
        .requestResponse(request)
        .map(message -> dataCodec.decode(message, responseType));
  }

  /**
   * Issues request to service which returns stream of service messages back.
   *
   * @param request request message to send.
   * @return flux publisher of service responses.
   */
  public Flux<ServiceMessage> requestMany(ServiceMessage request) {
    return requestMany(request, null);
  }

  /**
   * Issues request to service which returns stream of service messages back.
   *
   * @param request request with given headers.
   * @param responseType type of responses.
   * @return flux publisher of service responses.
   */
  public Flux<ServiceMessage> requestMany(ServiceMessage request, Class<?> responseType) {
    String qualifier = request.qualifier();
    if (serviceHandlers.contains(qualifier)) { // local service.
      return serviceHandlers.requestStream(request)
          .onErrorMap(ExceptionProcessor::mapException);
    } else { // remote service.
      return requestMany(request, responseType, addressLookup(request));
    }
  }

  /**
   * Given an address issues request to remote service which returns stream of service messages back.
   *
   * @param request request with given headers.
   * @param responseType type of responses.
   * @param address of remote target service to invoke.
   * @return flux publisher of service responses.
   */
  public Flux<ServiceMessage> requestMany(ServiceMessage request, Class<?> responseType, Address address) {
    requireNonNull(address, "requestMany address paramter is required and must not be null");
    return transport.create(address)
        .requestStream(request)
        .map(message -> dataCodec.decode(message, responseType));
  }

  /**
   * Issues stream of service requests to service which returns stream of service messages back.
   *
   * @param publisher of service requests.
   * @return flux publisher of service responses.
   */
  public Flux<ServiceMessage> requestBidirectional(Publisher<ServiceMessage> publisher) {
    return requestBidirectional(publisher, null);
  }

  /**
   * Issues stream of service requests to service which returns stream of service messages back.
   *
   * @param publisher of service requests.
   * @param responseType type of responses.
   * @return flux publisher of service responses.
   */
  public Flux<ServiceMessage> requestBidirectional(Publisher<ServiceMessage> publisher, Class<?> responseType) {
    return Flux.from(HeadAndTail.createFrom(publisher)).flatMap(pair -> {
      ServiceMessage request = pair.head();
      String qualifier = request.qualifier();

      Flux<ServiceMessage> publisher1 = Flux.from(pair.tail()).startWith(request);

      if (serviceHandlers.contains(qualifier)) { // local service.
        return serviceHandlers.requestChannel(publisher1)
            .onErrorMap(ExceptionProcessor::mapException);
      } else { // remote service.
        return requestBidirectional(publisher1, responseType, addressLookup(request));
      }
    });
  }

  /**
   * Given an address issues stream of service requests to service which returns stream of service messages back.
   *
   * @param publisher of service requests.
   * @param responseType type of responses.
   * @param address of remote target service to invoke.
   * @return flux publisher of service responses.
   */
  public Flux<ServiceMessage> requestBidirectional(Publisher<ServiceMessage> publisher, Class<?> responseType,
      Address address) {
    requireNonNull(address, "requestBidirectional address paramter is required and must not be null");
    return transport.create(address)
        .requestChannel(publisher)
        .map(message -> dataCodec.decode(message, responseType));

  }

  /**
   * Create proxy creates a java generic proxy instance by a given service interface.
   *
   * @param serviceInterface Service Interface type.
   * @return newly created service proxy object.
   */
  @SuppressWarnings("unchecked")
  public <T> T api(Class<T> serviceInterface) {

    final ServiceCall serviceCall = this;
    final Map<Method, MethodInfo> genericReturnTypes = Reflect.methodsInfo(serviceInterface);

    // noinspection unchecked
    return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {serviceInterface},
        (proxy, method, params) -> {
          MethodInfo methodInfo = genericReturnTypes.get(method);

          Optional<Object> check = toStringOrEqualsOrHashCode(method.getName(), serviceInterface, params);
          if (check.isPresent()) {
            return check.get(); // toString, hashCode was invoked.
          }
          Class<?> returnType = methodInfo.parameterizedReturnType();
          Metrics.mark(serviceInterface, metrics, method, "request");

          switch (methodInfo.communicationMode()) {
            case FIRE_AND_FORGET:
              return serviceCall.oneWay(toServiceMessage(method, params, methodInfo));

            case REQUEST_RESPONSE:
              return serviceCall.requestOne(toServiceMessage(method, params, methodInfo), returnType)
                  .transform(asMono(methodInfo));

            case REQUEST_STREAM:
              return serviceCall.requestMany(toServiceMessage(method, params, methodInfo), returnType)
                  .transform(asFlux(methodInfo));

            case REQUEST_CHANNEL:
              // if this is REQUEST_CHANNEL it means params[0] must be publisher thus its safe to cast.
              Flux<ServiceMessage> request = Flux.from((Publisher) params[0])
                  .map(data -> toServiceMessage(method, params, methodInfo));

              return serviceCall.requestBidirectional(request, returnType)
                  .transform(asFlux(methodInfo));

            default:
              throw new IllegalArgumentException("Communication mode is not supported: " + method);
          }
        });
  }

  private Address addressLookup(ServiceMessage request) {
    ServiceReference serviceReference =
        router.route(serviceRegistry, request)
            .orElseThrow(() -> noReachableMemberException(request));

    return Address.create(serviceReference.host(), serviceReference.port());
  }

  private static Function<? super Flux<ServiceMessage>, ? extends Publisher<ServiceMessage>> asFlux(
      MethodInfo methodInfo) {
    return flux -> methodInfo.isRequestTypeServiceMessage()
        ? flux
        : flux.map(ServiceMessage::data);
  }

  private static Function<? super Mono<ServiceMessage>, ? extends Publisher<ServiceMessage>> asMono(
      MethodInfo methodInfo) {
    return mono -> methodInfo.isRequestTypeServiceMessage()
        ? mono
        : mono.map(ServiceMessage::data);
  }

  private static ServiceMessage toServiceMessage(Method method, Object[] params, MethodInfo methodInfo) {
    return ServiceMessage.builder()
        .qualifier(methodInfo.serviceName(), method.getName())
        .data(method.getParameterCount() != 0 ? params[0] : NullData.NULL_DATA)
        .build();
  }

  private static ServiceUnavailableException noReachableMemberException(ServiceMessage request) {
    LOGGER.error("Failed  to invoke service, No reachable member with such service definition [{}], args [{}]",
        request.qualifier(), request);
    return new ServiceUnavailableException("No reachable member with such service: " + request.qualifier());
  }

  /**
   * check and handle toString or equals or hashcode method where invoked.
   * 
   * @param method that was invoked.
   * @param serviceInterface for a given service interface.
   * @param args parameters that where invoked.
   * @return Optional object as result of to string equals or hashCode result or absent if none of these where invoked.
   */
  private static Optional<Object> toStringOrEqualsOrHashCode(String method, Class<?> serviceInterface,
      Object... args) {

    switch (method) {
      case "toString":
        return Optional.of(serviceInterface.toString());
      case "equals":
        return Optional.of(serviceInterface.equals(args[0]));
      case "hashCode":
        return Optional.of(serviceInterface.hashCode());

      default:
        return Optional.empty();
    }
  }
}
