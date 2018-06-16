package io.scalecube.services.spring;

import io.scalecube.services.Microservices;
import io.scalecube.transport.Address;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Application {

  public static void main(String[] args) throws InterruptedException {
    Microservices seed = Microservices.builder().startAwait();

    Address address = seed.cluster().address();

    Microservices remoteNode = Microservices.builder()
        .seeds(address)
        .services(new RemoteServiceImpl())
        .startAwait();

    System.setProperty("SEEDS", address.toString());

    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);

    SpringService springService = context.getBean(SpringService.class);
    SomeComponent someComponent = context.getBean(SomeComponent.class);

    Microservices node = Microservices.builder()
        .seeds(address)
        .services(springService)
        .startAwait();

    RemoteService remoteServiceApi = node.call().create().api(RemoteService.class);
    someComponent.setRemoteService(remoteServiceApi);

    SpringService springServiceApi = seed.call().create().api(SpringService.class);

    springServiceApi.invoke().subscribe(System.err::println);

    Thread.currentThread().join();
  }
}
