package io.scalecube.services.example;

import io.scalecube.services.annotations.ServiceMethod;

import reactor.core.publisher.Mono;


/**
 * this is a base interface and holds no @Service annotation as the service implementing it will provide the @Service
 * annotation.
 */
public interface BaseInterface {

  @ServiceMethod
  Mono<String> helloFromBase(String name);

}
