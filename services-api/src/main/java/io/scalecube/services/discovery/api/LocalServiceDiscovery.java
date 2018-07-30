package io.scalecube.services.discovery.api;

import io.scalecube.services.ServiceEndpoint;
import io.scalecube.transport.Address;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LocalServiceDiscovery implements ServiceDiscovery {

  private Address address;
  private ServiceEndpoint endpoint;

  @Override
  public Mono<ServiceDiscovery> start(DiscoveryConfig discoveryConfig) {
    this.address =  Address.create("localhost", 0); 
    this.endpoint = discoveryConfig.endpoint();
    return Mono.just(this);
  }

  @Override
  public Mono<Void> shutdown() {
    return  Mono.just(this).then();
  }

  @Override
  public Address address() {
    return address;
  }

  @Override
  public ServiceEndpoint endpoint() {
    return endpoint;
  }

  @Override
  public Flux<DiscoveryEvent> listen() {
    return Flux.empty();
  }

}
