package aeron.playground;

import io.rsocket.AbstractRSocket;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TestRSocket implements SocketAcceptor {
  @Override
  public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
    return Mono.just(new AbstractRSocket() {
      @Override
      public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        // FIXME: need to seek handler and invoke it.
        throw new UnsupportedOperationException();
      }

      @Override
      public Flux<Payload> requestStream(Payload payload) {
        return Flux.just(payload);
      }

      @Override
      public Mono<Payload> requestResponse(Payload payload) {
        System.out.println("hello");
        return Mono.just(payload);
      }

      @Override
      public Mono<Void> fireAndForget(Payload payload) {
        System.out.println("hello");
        return Mono.empty();
      }
    });
  }
}
