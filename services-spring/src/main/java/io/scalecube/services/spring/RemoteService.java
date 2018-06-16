package io.scalecube.services.spring;

import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;

import reactor.core.publisher.Mono;

@Service
public interface RemoteService {

  @ServiceMethod
  Mono<String> ping();
}
