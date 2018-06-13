package io.scalecube.services.transport.rsocket;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.ByteBufPayload;

import reactor.core.publisher.Mono;

public class RSocketServer {
  public static void main(String[] args) throws InterruptedException {
    NettyContextCloseable nettyContextCloseable =
        RSocketFactory.receive().acceptor((setup, sendingSocket) -> Mono.just(new AbstractRSocket() {
          @Override
          public Mono<Payload> requestResponse(Payload payload) {
            System.out.println("RCV:" + ByteBufPayload.create(payload).getDataUtf8());
            return Mono.never();
          }
        })).transport(TcpServerTransport.create(4001)).start().block();
    System.err.println(nettyContextCloseable);
    Thread.currentThread().join();
  }
}
