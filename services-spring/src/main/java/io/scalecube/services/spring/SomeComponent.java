package io.scalecube.services.spring;

import io.scalecube.services.annotations.Inject;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class SomeComponent {
  @Inject
  private RemoteService remoteService;

  public Mono<String> ping() {
    return remoteService.ping();
  }

  public void setRemoteService(RemoteService remoteService) {
    this.remoteService = remoteService;
  }
}
