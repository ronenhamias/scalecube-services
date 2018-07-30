package io.scalecube.examples;

import io.scalecube.services.Microservices;
import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Basic getting started example
 */
public class BootstrapExample {

  public static void main(String[] args) throws Exception {
    System.out.println("Start HelloWorldService with BusinessLogicFacade");
    Microservices node1 = Microservices.builder()
        .services(call -> Collections.singletonList(
            new HelloWorldServiceImpl(
                new BusinessLogicFacade(
                    call.create().api(ServiceHello.class),
                    call.create().api(ServiceWorld.class)))))
        .startAwait();

    System.out.println("Start ServiceHello");
    Microservices node2 = Microservices.builder()
        .seeds(node1.discovery().address())
        .services(new ServiceHelloImpl()).startAwait();

    System.out.println("Start ServiceWorld");
    Microservices node3 = Microservices.builder()
        .seeds(node1.discovery().address())
        .services(new ServiceWorldImpl()).startAwait();

    System.out.println("Wait for some time so nodes could catch up with each other ...");
    TimeUnit.SECONDS.sleep(3);

    System.out.println("Making hello world business logic ...");
    HelloWorldService helloWorldService = node1.call().create().api(HelloWorldService.class);

    String helloWorld = helloWorldService.helloWorld().block(Duration.ofSeconds(6));
    System.out.println("Result of calling hello world business logic is ... => " + helloWorld);

    Mono.when(node1.shutdown(), node2.shutdown(), node3.shutdown()).block(Duration.ofSeconds(3));
  }

  /**
   * Just service
   */
  @Service
  public interface ServiceHello {
    @ServiceMethod
    Mono<String> hello();
  }

  /**
   * Just service
   */
  @Service
  public interface ServiceWorld {
    @ServiceMethod
    Mono<String> world();
  }

  /**
   * Facade service for calling another services
   */
  @Service
  public interface HelloWorldService {
    @ServiceMethod
    Mono<String> helloWorld();
  }

  public static class ServiceHelloImpl implements ServiceHello {
    @Override
    public Mono<String> hello() {
      return Mono.just("hello");
    }
  }

  public static class ServiceWorldImpl implements ServiceWorld {
    @Override
    public Mono<String> world() {
      return Mono.just("world");
    }
  }

  public static class HelloWorldServiceImpl implements HelloWorldService {
    private final BusinessLogicFacade businessLogicFacade;

    public HelloWorldServiceImpl(BusinessLogicFacade businessLogicFacade) {
      this.businessLogicFacade = businessLogicFacade;
    }

    @Override
    public Mono<String> helloWorld() {
      return businessLogicFacade.helloWorld();
    }
  }

  /**
   * POJO facade for calling other services, aggregating their responses and doing business logic.
   */
  public static class BusinessLogicFacade {
    private final ServiceHello serviceHello;
    private final ServiceWorld serviceWorld;

    public BusinessLogicFacade(ServiceHello serviceHello, ServiceWorld serviceWorld) {
      this.serviceHello = serviceHello;
      this.serviceWorld = serviceWorld;
    }

    Mono<String> helloWorld() {
      BiFunction<String, String, String> businessLogic = String::concat;
      return Flux.zip(serviceHello.hello(), serviceWorld.world(), businessLogic).as(Mono::from);
    }
  }
}
