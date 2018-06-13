package io.scalecube.services.transport.rsocket;

import io.scalecube.services.codec.HeadersCodec;
import io.scalecube.services.codec.ServiceMessageCodec;
import io.scalecube.services.transport.rsocket.client.RSocketClientTransport;
import io.scalecube.services.transport.rsocket.client.RSocketServiceClientAdapter;
import io.scalecube.transport.Address;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.ByteBufPayload;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RSocketClientTransportTest {


  @Test
  void testConnectionDrop() throws InterruptedException {

    NettyContextCloseable server = startRsocketServer();
    InetSocketAddress inetAddress = server.address();
    Address address = Address.create(inetAddress.getHostName(), inetAddress.getPort());
    RSocketClientTransport transport = new RSocketClientTransport(null);
    RSocketServiceClientAdapter clientChannel = (RSocketServiceClientAdapter) transport.create(address);

    Publisher<RSocket> source = clientChannel.getrSocket();
    Flux.from(source).log("LOGGING FLUX:").subscribe();
    RSocket block = Mono.from(source).block(Duration.ofSeconds(5));
    Assertions.assertNotNull(block);

//    // subscribe to errors
//    CountDownLatch disposed = new CountDownLatch(1);
//    Flux.from(source).doOnError(e -> disposed.countDown()).subscribe();
//
//    server.dispose();
//
//    Assertions.assertTrue(disposed.await(5, TimeUnit.SECONDS));

      Thread.currentThread().join();
  }

  private NettyContextCloseable startRsocketServer() {
    return RSocketFactory.receive().acceptor((setup, sendingSocket) -> Mono.just(new AbstractRSocket() {
      @Override
      public Mono<Payload> requestResponse(Payload payload) {
        System.out.println("RCV:" + ByteBufPayload.create(payload).getDataUtf8());
        return Mono.just(ByteBufPayload.create("RESPONSE"));
      }
    }))
        .transport(TcpServerTransport.create(4001))
        .start().block();
  }
}
