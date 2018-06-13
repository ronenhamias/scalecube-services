package io.scalecube.services.transport.rsocket;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;

import java.io.IOException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

public class RSocketClient {

  public static final Payload PAYLOAD = DefaultPayload.create("hola");

  public static void main(String[] args) throws InterruptedException {
    Mono<RSocket> rsocketMono = RSocketFactory.connect()
        .transport(TcpClientTransport.create(4001))
        .start();

    Flux<RSocket> connectionFlux = Flux.create((FluxSink<RSocket> sink) -> {
      rsocketMono.subscribe(rSocket -> {
        rSocket.onClose()
            .log("ON_CLOSE: ")
            .doOnTerminate(() -> sink.error(new IOException("Disconnected")))
            .subscribe();
        sink.next(rSocket);
      }, sink::error/* , sink::complete */);
    }).log("CONNECTION_FLUX");

    connectionFlux.flatMap(rsocket -> rsocket.requestResponse(PAYLOAD).log("REQ_RESP: ")).subscribe();

    Thread.currentThread().join();
  }
}
