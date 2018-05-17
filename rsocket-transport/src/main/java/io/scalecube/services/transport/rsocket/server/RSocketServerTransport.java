package io.scalecube.services.transport.rsocket.server;

import io.scalecube.services.api.ServiceMessageHandler;
import io.scalecube.services.codec.ServiceMessageCodec;
import io.scalecube.services.transport.server.api.ServerTransport;

import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.TcpServerTransport;

import java.net.InetSocketAddress;

import reactor.core.publisher.Mono;

public class RSocketServerTransport implements ServerTransport {

  private final ServiceMessageCodec codec;

  private NettyContextCloseable server;

  public RSocketServerTransport(ServiceMessageCodec codec) {
    this.codec = codec;
  }

  @Override
  public InetSocketAddress bindAwait(InetSocketAddress address, ServiceMessageHandler acceptor) {

    TcpServerTransport transport = TcpServerTransport.create(address);

    this.server = RSocketFactory.receive()
        .acceptor(new RSocketServiceAcceptor(acceptor, codec))
        .transport(transport)
        .start()
        .block();

    return server.address();
  }

  @Override
  public Mono<Void> stop() {
    if (server != null) {
      server.dispose();
      return server.onClose();
    } else {
      return Mono.empty();
    }
  }
}
