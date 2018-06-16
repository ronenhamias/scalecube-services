package io.scalecube.services.spring;

import io.scalecube.services.Microservices;
import io.scalecube.services.annotations.Inject;
import io.scalecube.transport.Address;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Field;

public class Application {

  public static void main(String[] args) throws Exception {
    Microservices seed = Microservices.builder().startAwait();

    Address address = seed.cluster().address();

    Microservices remoteNode = Microservices.builder()
        .seeds(address)
        .services(new RemoteServiceImpl())
        .startAwait();

    System.setProperty("SEEDS", address.toString());

    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);

    SpringService springService = context.getBean(SpringService.class);

    Microservices node = Microservices.builder()
        .seeds(address)
        .services(springService)
        .startAwait();

    SomeComponent someComponent = context.getBean(SomeComponent.class);
    RemoteService remoteServiceApi = node.call().create().api(RemoteService.class);
    // someComponent.setRemoteService(remoteServiceApi);

    for (Field field : someComponent.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(Inject.class)) {
        field.setAccessible(true);
        field.set(someComponent, remoteServiceApi);
      }
    }

    SpringService springServiceApi = seed.call().create().api(SpringService.class);

    springServiceApi.invoke().subscribe(System.err::println, System.err::println);

    Thread.currentThread().join();
  }
}
