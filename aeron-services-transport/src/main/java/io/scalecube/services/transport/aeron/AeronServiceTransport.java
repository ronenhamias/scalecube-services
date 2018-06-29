package io.scalecube.services.transport.aeron;

import io.scalecube.services.codec.HeadersCodec;
import io.scalecube.services.codec.ServiceMessageCodec;
import io.scalecube.services.transport.ServiceTransport;
import io.scalecube.services.transport.aeront.client.AeronClientTransport;
import io.scalecube.services.transport.aeront.server.AeronServerTransport;
import io.scalecube.services.transport.client.api.ClientTransport;
import io.scalecube.services.transport.server.api.ServerTransport;

public class AeronServiceTransport implements ServiceTransport {

  private static final String DEFAULT_HEADERS_FORMAT = "application/json";

  @Override
  public ClientTransport getClientTransport() {
    HeadersCodec headersCodec = HeadersCodec.getInstance(DEFAULT_HEADERS_FORMAT);
    return new AeronClientTransport(new ServiceMessageCodec(headersCodec));
  }

  @Override
  public ServerTransport getServerTransport() {
    HeadersCodec headersCodec = HeadersCodec.getInstance(DEFAULT_HEADERS_FORMAT);
    return new AeronServerTransport(new ServiceMessageCodec(headersCodec));
  }
}
