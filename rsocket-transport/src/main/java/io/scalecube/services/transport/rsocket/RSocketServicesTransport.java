package io.scalecube.services.transport.rsocket;

import io.scalecube.services.transport.ServiceTransport;
import io.scalecube.services.transport.client.api.ClientTransport;
import io.scalecube.services.transport.rsocket.client.RSocketClientTransport;
import io.scalecube.services.transport.rsocket.server.RSocketServerTransport;
import io.scalecube.services.transport.server.api.ServerTransport;

public class RSocketServicesTransport implements ServiceTransport{

  private PayloadCodec payloadCodec;

  public RSocketServicesTransport(PayloadCodec payloadCodec) {
    this.payloadCodec = payloadCodec;
  }

  @Override
  public ClientTransport getClientTransport() {
    return new RSocketClientTransport(payloadCodec);
  }

  @Override
  public ServerTransport getServerTransport() {
    return new RSocketServerTransport(payloadCodec);
  }

}