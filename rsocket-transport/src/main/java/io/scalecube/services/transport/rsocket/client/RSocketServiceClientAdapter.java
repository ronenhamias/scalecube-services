package io.scalecube.services.transport.rsocket.client;

import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.codec.ServiceMessageCodec;
import io.scalecube.services.transport.client.api.ClientChannel;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.ByteBufPayload;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

public class RSocketServiceClientAdapter implements ClientChannel {

  private Publisher<RSocket> rSocket;
  private ServiceMessageCodec messageCodec;

  public RSocketServiceClientAdapter(Publisher<RSocket> rSocket, ServiceMessageCodec codec) {
    this.rSocket = rSocket;
    this.messageCodec = codec;
  }

  @Override
  public Mono<ServiceMessage> requestResponse(ServiceMessage message) {
    return Mono.from(rSocket)
        .flatMap(rSocket -> Mono.create((MonoSink<Payload> emitter) -> {
          rSocket.onClose().subscribe(

              aVoid -> {
              },
              throwable -> {
              },
              () -> {
                emitter.error(new RuntimeException("### onComplete"));
              });

          rSocket.requestResponse(toPayload(message))
              .subscribe(emitter::success, emitter::error, emitter::success);
        }))
        .map(this::toMessage);
  }

  @Override
  public Flux<ServiceMessage> requestStream(ServiceMessage message) {
    return Flux.from(rSocket)
        .flatMap(rSocket -> Flux.create((FluxSink<Payload> emitter) -> {
          rSocket.onClose().subscribe(

              aVoid -> {
              },
              throwable -> {
              },
              () -> {
                emitter.error(new RuntimeException("### onComplete"));
              });

          rSocket.requestStream(toPayload(message))
              .subscribe(emitter::next, emitter::error, emitter::complete);
        }))
        .map(this::toMessage);
  }

  @Override
  public Flux<ServiceMessage> requestChannel(Publisher<ServiceMessage> publisher) {
    return Flux.from(rSocket)
        .flatMap(rSocket -> rSocket.requestChannel(Flux.from(publisher).map(this::toPayload)))
        .map(this::toMessage);
  }

  private Payload toPayload(ServiceMessage request) {
    return messageCodec.encodeAndTransform(request, ByteBufPayload::create);
  }

  private ServiceMessage toMessage(Payload payload) {
    return messageCodec.decode(payload.sliceData(), payload.sliceMetadata());
  }
}
