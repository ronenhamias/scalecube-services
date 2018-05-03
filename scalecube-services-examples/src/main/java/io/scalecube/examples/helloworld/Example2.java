package io.scalecube.examples.helloworld;

import io.scalecube.examples.helloworld.service.GreetingServiceImpl;
import io.scalecube.examples.helloworld.service.api.Greeting;
import io.scalecube.services.Microservices;
import io.scalecube.services.ServiceCall.Call;
import io.scalecube.services.api.ServiceMessage;

import reactor.core.publisher.Mono;

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
public class Example2 {

  
  public static void main(String[] args) {
    Microservices seed = Microservices.builder().build();

    Microservices microservices = Microservices.builder()
        .seeds(seed.cluster().address())
        .services(new GreetingServiceImpl())
        .build();

    Call service = seed.call();
    
    Mono.from(
        service.requestOne(ServiceMessage.builder()
        .qualifier("io.scalecube.Greetings/sayHello")
        .data("joe").build(), Greeting.class))
    
    .subscribe(consumer -> {
      Greeting greeting = consumer.data();
      System.out.println(greeting.message());
    });

    seed.shutdown().block();
    microservices.shutdown().block();
    
  }

}
