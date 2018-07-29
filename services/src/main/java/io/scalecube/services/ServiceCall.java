package io.scalecube.services;

import static java.util.Objects.requireNonNull;

import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.codec.ServiceMessageCodec;
import io.scalecube.services.exceptions.ExceptionProcessor;
import io.scalecube.services.exceptions.ServiceUnavailableException;
import io.scalecube.services.methods.MethodInfo;
import io.scalecube.services.methods.ServiceMethodRegistry;
import io.scalecube.services.methods.ServiceMethodRegistryImpl;
import io.scalecube.services.metrics.Metrics;
import io.scalecube.services.registry.ServiceRegistryImpl;
import io.scalecube.services.registry.api.ServiceRegistry;
import io.scalecube.services.routing.RoundRobinServiceRouter;
import io.scalecube.services.routing.Router;
import io.scalecube.services.routing.Routers;
import io.scalecube.services.transport.ServiceTransport;
import io.scalecube.services.transport.client.api.ClientTransport;
import io.scalecube.transport.Address;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ServiceCall {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCall.class);
  private static final ClientTransport client = ServiceTransport.getTransport().getClientTransport();

  private final ClientTransport transport;
  private final ServiceMethodRegistry methodRegistry;
  private final ServiceRegistry serviceRegistry;
  private final Router router;
  private final Metrics metrics;
  private Address[] address;

  ServiceCall(Call call) {
    this.transport = call.transport;
    this.methodRegistry = call.methodRegistry;
    this.serviceRegistry = call.serviceRegistry;
    this.router = call.router;
    this.metrics = call.metrics;
    this.address = call.address;
  }

  public static Call client() {
    ServiceMethodRegistry methodRegistry = new ServiceMethodRegistryImpl();
    return new Call(client, methodRegistry, new ServiceRegistryImpl());
  }

  public static class Call {

    private Router router = Routers.getRouter(RoundRobinServiceRouter.class);
    private Metrics metrics;

    private final ClientTransport transport;
    private final ServiceMethodRegistry methodRegistry;
    private final ServiceRegistry serviceRegistry;
    private Address[] address;

    public Call(ClientTransport transport,
        ServiceMethodRegistry methodRegistry,
        ServiceRegistry serviceRegistry) {
      this.transport = transport;
      this.serviceRegistry = serviceRegistry;
      this.methodRegistry = methodRegistry;
    }

    public Call address(Address... address) {
      this.address = address;
      return this;
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
   * Issues fire-and-forget request.
   *
   * @param request request message to send.
   * @return mono publisher completing normally or with error.
   */
  public Mono<Void> oneWay(ServiceMessage request) {
    return requestOne(request, Void.class).then();
  }

  /**
   * Issues fire-and-forget request.
   *
   * @param request request message to send.
   * @param address of remote target service to invoke.
   * @return mono publisher completing normally or with error.
   */
  public Mono<Void> oneWay(ServiceMessage request, Address address) {
    return requestOne(request, Void.class, address).then();
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
    if (methodRegistry.containsInvoker(qualifier)) { // local service.
      return methodRegistry.getInvoker(request.qualifier())
          .invokeOne(request, ServiceMessageCodec::decodeData)
          .onErrorMap(ExceptionProcessor::mapException);
    } else { // remote service.
      return addressLookup(request)
          .flatMap(address -> requestOne(request, responseType, address));
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
        .map(message -> ServiceMessageCodec.decodeData(message, responseType));
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
    if (methodRegistry.containsInvoker(qualifier)) { // local service.
      return methodRegistry.getInvoker(request.qualifier())
          .invokeMany(request, ServiceMessageCodec::decodeData)
          .onErrorMap(ExceptionProcessor::mapException);
    } else { // remote service.
      return addressLookup(request)
          .flatMapMany(address -> requestMany(request, responseType, address));
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
        .map(message -> ServiceMessageCodec.decodeData(message, responseType));
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
      Flux<ServiceMessage> messages = Flux.from(pair.tail()).startWith(request);

      if (methodRegistry.containsInvoker(qualifier)) { // local service.
        return methodRegistry.getInvoker(qualifier)
            .invokeBidirectional(messages, ServiceMessageCodec::decodeData)
            .onErrorMap(ExceptionProcessor::mapException);
      } else { // remote service.
        return addressLookup(request)
            .flatMapMany(address -> requestBidirectional(messages, responseType, address));
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
        .map(message -> ServiceMessageCodec.decodeData(message, responseType));

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
          final MethodInfo methodInfo = genericReturnTypes.get(method);
          final Class<?> returnType = methodInfo.parameterizedReturnType();
          final boolean isServiceMessage = methodInfo.isRequestTypeServiceMessage();

          Optional<Object> check = toStringOrEqualsOrHashCode(method.getName(), serviceInterface, params);
          if (check.isPresent()) {
            return check.get(); // toString, hashCode was invoked.
          }

          Metrics.mark(serviceInterface, metrics, method, "request");

          switch (methodInfo.communicationMode()) {
            case FIRE_AND_FORGET:
              return serviceCall.oneWay(toServiceMessage(methodInfo, params));

            case REQUEST_RESPONSE:
              return serviceCall.requestOne(toServiceMessage(methodInfo, params), returnType)
                  .transform(asMono(isServiceMessage));

            case REQUEST_STREAM:
              return serviceCall.requestMany(toServiceMessage(methodInfo, params), returnType)
                  .transform(asFlux(isServiceMessage));

            case REQUEST_CHANNEL:
              // this is REQUEST_CHANNEL so it means params[0] must be a publisher - its safe to cast.
              return serviceCall.requestBidirectional(Flux.from((Publisher) params[0])
                  .map(data -> toServiceMessage(methodInfo, data)), returnType)
                  .transform(asFlux(isServiceMessage));

            default:
              throw new IllegalArgumentException("Communication mode is not supported: " + method);
          }
        });
  }

  private Mono<Address> addressLookup(ServiceMessage request) {
    if (this.address != null && this.address.length > 0) {
      return router.route(serviceRegistry, request)
          .map(serviceReference -> Mono.just(Address.create(serviceReference.host(), serviceReference.port())))
          .orElseGet(() -> Mono.error(noReachableMemberException(request)));
    } else {
      // TODO: apply router in case there is more then one possible address
      return Mono.just(this.address[0]);
    }
  }

  private static ServiceMessage toServiceMessage(MethodInfo methodInfo, Object... params) {
    return ServiceMessage.builder()
        .qualifier(methodInfo.serviceName(), methodInfo.methodName())
        .data(methodInfo.parameterCount() != 0 ? params[0] : null)
        .build();
  }

  private static Function<? super Flux<ServiceMessage>, ? extends Publisher<ServiceMessage>> asFlux(
      boolean isRequestTypeServiceMessage) {
    return flux -> isRequestTypeServiceMessage ? flux : flux.filter(ServiceMessage::hasData).map(ServiceMessage::data);
  }

  private static Function<? super Mono<ServiceMessage>, ? extends Publisher<ServiceMessage>> asMono(
      boolean isRequestTypeServiceMessage) {
    return mono -> isRequestTypeServiceMessage ? mono : mono.filter(ServiceMessage::hasData).map(ServiceMessage::data);
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
