package io.scalecube.services.spring;

import reactor.core.publisher.Mono;

public class RemoteServiceImpl implements RemoteService {

  @Override
  public Mono<String> ping() {
    return Mono.just("RemoteServiceImpl");
  }

}
