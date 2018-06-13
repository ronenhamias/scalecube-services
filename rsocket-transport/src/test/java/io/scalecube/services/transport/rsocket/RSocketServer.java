package io.scalecube.services.transport.rsocket;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.ByteBufPayload;

import java.time.Duration;

import reactor.core.publisher.Flux;
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

          @Override
          public Flux<Payload> requestStream(Payload payload) {
            System.out.println("RCV:" + ByteBufPayload.create(payload).getDataUtf8());
            return Flux.interval(Duration.ofSeconds(1)).doOnEach(System.out::println).map(i -> payload);
          }
        })).transport(TcpServerTransport.create(4001)).start().block();
    System.err.println(nettyContextCloseable);
    Thread.currentThread().join();
  }
}
