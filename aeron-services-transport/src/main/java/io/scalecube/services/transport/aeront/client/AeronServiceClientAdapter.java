package io.scalecube.services.transport.aeront.client;

import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.codec.ServiceMessageCodec;
import io.scalecube.services.transport.client.api.ClientChannel;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AeronServiceClientAdapter implements ClientChannel {

  
  private ServiceMessageCodec messageCodec;

  public AeronServiceClientAdapter(ServiceMessageCodec codec) {
    this.messageCodec = codec;
  }

  @Override
  public Mono<ServiceMessage> requestResponse(ServiceMessage message) {
    return null;
    
  }

  @Override
  public Flux<ServiceMessage> requestStream(ServiceMessage message) {
    return null;
    
  }

  @Override
  public Flux<ServiceMessage> requestChannel(Publisher<ServiceMessage> publisher) {
    return null;
   
  }
}
