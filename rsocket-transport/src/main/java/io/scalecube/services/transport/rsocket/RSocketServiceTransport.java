package io.scalecube.services.transport.rsocket;

import io.netty.channel.EventLoopGroup;
import io.scalecube.services.codec.HeadersCodec;
import io.scalecube.services.codec.ServiceMessageCodec;
import io.scalecube.services.transport.ServiceTransport;
import io.scalecube.services.transport.ServiceTransportConfig;
import io.scalecube.services.transport.client.api.ClientTransport;
import io.scalecube.services.transport.rsocket.client.RSocketClientTransport;
import io.scalecube.services.transport.rsocket.server.RSocketServerTransport;
import io.scalecube.services.transport.server.api.ServerTransport;

import java.util.Optional;

public class RSocketServiceTransport implements ServiceTransport {

  private static final String DEFAULT_HEADERS_FORMAT = "application/json";

  private EventLoopGroup customClientEventLoopGroup;
  private EventLoopGroup customServerEventLoopGroup;

  @Override
  public void configure(ServiceTransportConfig config) {
      this.customClientEventLoopGroup = (EventLoopGroup) config.clientEventLoopGroup();
      this.customServerEventLoopGroup = (EventLoopGroup) config.serverEventLoopGroup();
  }

  @Override
  public ClientTransport getClientTransport() {
    HeadersCodec headersCodec = HeadersCodec.getInstance(DEFAULT_HEADERS_FORMAT);
    return new RSocketClientTransport(new ServiceMessageCodec(headersCodec), Optional.ofNullable(customClientEventLoopGroup));
  }

  @Override
  public ServerTransport getServerTransport() {
    HeadersCodec headersCodec = HeadersCodec.getInstance(DEFAULT_HEADERS_FORMAT);
    return new RSocketServerTransport(new ServiceMessageCodec(headersCodec), Optional.ofNullable(customServerEventLoopGroup));
  }



}
