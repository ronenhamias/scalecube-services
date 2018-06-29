package io.scalecube.services.transport.aeront.server;

import io.scalecube.services.codec.ServiceMessageCodec;
import io.scalecube.services.methods.ServiceMethodRegistry;
import io.scalecube.services.transport.server.api.ServerTransport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

public class AeronServerTransport implements ServerTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(AeronServerTransport.class);

  private final ServiceMessageCodec codec;

  public AeronServerTransport(ServiceMessageCodec codec) {
    this.codec = codec;
  }

  @Override
  public InetSocketAddress bindAwait(InetSocketAddress address, ServiceMethodRegistry methodRegistry) {
    return address;
  
  }

  @Override
  public Mono<Void> stop() {
    return null;
  }
}
