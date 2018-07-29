package io.scalecube.examples.helloworld;

import io.scalecube.examples.helloworld.service.GreetingServiceImpl;
import io.scalecube.examples.helloworld.service.api.GreetingsService;
import io.scalecube.services.Microservices;
import io.scalecube.services.ServiceCall;
import io.scalecube.transport.Address;

/**
 * The Hello World project is a time-honored tradition in computer programming. It is a simple exercise that gets you
 * started when learning something new. Let’s get started with ScaleCube!
 * <p>
 * the example starts 1 node. The
 * <code>microservices</code> variable is a member that disable discovery <code>GreetingService</code>
 * instance.
 */
public class Example3 {

  public static void main(String[] args) {

    // Construct a ScaleCube node which joins the cluster hosting the Greeting Service
    Microservices microservices = Microservices.builder()
        .disableDiscovery()
        .servicePort(9090)
        .services(new GreetingServiceImpl())
        .startAwait();

    ServiceCall.client().address(Address.create("locahost", 9090));
    
    // Create service proxy
    GreetingsService service = microservices.call().create().api(GreetingsService.class);

    // Execute the services and subscribe to service events
    service.sayHello("joe").subscribe(consumer -> {
      System.out.println(consumer.message());
    });

    // shut down the nodes
    microservices.shutdown().block();
  }
}
