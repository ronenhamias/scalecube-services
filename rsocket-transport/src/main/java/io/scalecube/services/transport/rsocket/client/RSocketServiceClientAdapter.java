package io.scalecube.services.transport.rsocket.client;

import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.codec.ServiceMessageCodec;
import io.scalecube.services.exceptions.ConnectionClosedException;
import io.scalecube.services.transport.client.api.ClientChannel;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.ByteBufPayload;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RSocketServiceClientAdapter implements ClientChannel {

  private Mono<RSocket> rSocket;
  private ServiceMessageCodec messageCodec;

  public RSocketServiceClientAdapter(Mono<RSocket> rSocket, ServiceMessageCodec codec) {
    this.rSocket = rSocket;
    this.messageCodec = codec;
  }

  @Override
  public Mono<ServiceMessage> requestResponse(ServiceMessage message) {
    return rSocket
        .flatMap(rSocket -> rSocket.requestResponse(toPayload(message))
            .takeUntilOther(listenConnectionClose(rSocket)))
        .map(this::toMessage);
  }

  @Override
  public Flux<ServiceMessage> requestStream(ServiceMessage message) {
    return rSocket
        .flatMapMany(rSocket -> rSocket.requestStream(toPayload(message))
            .takeUntilOther(listenConnectionClose(rSocket)))
        .map(this::toMessage);
  }

  @Override
  public Flux<ServiceMessage> requestChannel(Publisher<ServiceMessage> publisher) {
    return rSocket
        .flatMapMany(rSocket -> rSocket
            .requestChannel(Flux.from(publisher).map(this::toPayload))
            .takeUntilOther(listenConnectionClose(rSocket)))
        .map(this::toMessage);
  }

  private Payload toPayload(ServiceMessage request) {
    return messageCodec.encodeAndTransform(request, ByteBufPayload::create);
  }

  private ServiceMessage toMessage(Payload payload) {
    return messageCodec.decode(payload.sliceData(), payload.sliceMetadata());
  }

  @SuppressWarnings("unchecked")
  private <T> Mono<T> listenConnectionClose(RSocket rSocket) {
    return rSocket.onClose()
        .map(aVoid -> (T) aVoid)
        .switchIfEmpty(Mono.defer(this::toConnectionClosedException));
  }

  private <T> Mono<T> toConnectionClosedException() {
    return Mono.error(new ConnectionClosedException("Connection closed"));
  }
}
