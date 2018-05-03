package io.scalecube.examples.helloworld;

import io.scalecube.examples.helloworld.service.GreetingServiceImpl;
import io.scalecube.examples.helloworld.service.api.GreetingsService;
import io.scalecube.services.Microservices;

/**
 * 
 * Basic Hello world application.
 * 
 * the example starts 2 cluster member nodes. 
 * 1. seed is a member node and holds no services of its own.
 * 2. microservices is a member that joins seed member and provision GreetingServiceImpl instance.
 * 
 * the
 */
public class Application {

  public static void main(String[] args) {
    Microservices seed = Microservices.builder().build();

    Microservices microservices = Microservices.builder()
        .seeds(seed.cluster().address())
        .services(new GreetingServiceImpl())
        .build();

    GreetingsService service = seed.call().api(GreetingsService.class);
    service.sayHello("joe").subscribe(consumer -> {
      System.out.println(consumer.message());
    });

    seed.shutdown().block();
    microservices.shutdown().block();
    
  }

}
