package io.scalecube.services.transport.aeront.client;

import io.scalecube.services.codec.ServiceMessageCodec;
import io.scalecube.services.transport.client.api.ClientChannel;
import io.scalecube.services.transport.client.api.ClientTransport;
import io.scalecube.transport.Address;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AeronClientTransport implements ClientTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(AeronClientTransport.class);

  private final ServiceMessageCodec codec;

  public AeronClientTransport(ServiceMessageCodec codec) {
    this.codec = codec;
  }

  @Override
  public ClientChannel create(Address address) {
    return null;
  }

}
