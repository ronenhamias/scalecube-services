package io.scalecube.services.routings;

import io.scalecube.services.GreetingRequest;
import io.scalecube.services.Microservices;
import io.scalecube.services.routings.examples.CanaryService;
import io.scalecube.services.routings.examples.GreetingServiceImplA;
import io.scalecube.services.routings.examples.GreetingServiceImplB;
import io.scalecube.services.routings.examples.WeightedRandomRouter;

import reactor.core.publisher.Mono;


public class ServiceTagsExample {

  public static void main(String[] args) {
    Microservices gateway = Microservices.builder().startAwait();

    Microservices services1 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .service(new GreetingServiceImplA()).tag("Weight", "0.3").register()
        .startAwait();

    Microservices services2 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .service(new GreetingServiceImplB()).tag("Weight", "0.7").register()
        .startAwait();

    CanaryService service = gateway.call()
        .router(WeightedRandomRouter.class)
        .create()
        .api(CanaryService.class);

    for (int i = 0; i < 10; i++) {
      Mono.from(service.greeting(new GreetingRequest("joe"))).doOnNext(success -> {
        success.getResult().startsWith("B");
        System.out.println(success);
      });
    }
  }

}
