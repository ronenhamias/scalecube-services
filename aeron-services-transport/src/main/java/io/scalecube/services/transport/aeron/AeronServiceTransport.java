package io.scalecube.services.transport.aeron;

import reactor.core.publisher.Mono;
import io.scalecube.services.codec.HeadersCodec;
import io.scalecube.services.codec.ServiceMessageCodec;
import io.scalecube.services.transport.ServiceTransport;
import io.scalecube.services.transport.aeront.client.AeronClientTransport;
import io.scalecube.services.transport.aeront.server.AeronServerTransport;
import io.scalecube.services.transport.client.api.ClientTransport;
import io.scalecube.services.transport.server.api.ServerTransport;
import java.util.concurrent.ExecutorService;

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

  @Override
  public ExecutorService getExecutorService() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Mono<Void> shutdown() {
    // TODO Auto-generated method stub
    return null;
  }
}
