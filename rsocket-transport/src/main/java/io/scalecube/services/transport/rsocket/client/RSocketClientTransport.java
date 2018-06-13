package io.scalecube.services.transport.rsocket.client;

import io.scalecube.services.codec.ServiceMessageCodec;
import io.scalecube.services.transport.client.api.ClientChannel;
import io.scalecube.services.transport.client.api.ClientTransport;
import io.scalecube.transport.Address;

import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.exceptions.ConnectionErrorException;
import io.rsocket.transport.netty.client.TcpClientTransport;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.tcp.TcpClient;

public class RSocketClientTransport implements ClientTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(RSocketClientTransport.class);

  private final Map<Address, Publisher<RSocket>> rSockets = new ConcurrentHashMap<>();

  private final ServiceMessageCodec codec;

  public RSocketClientTransport(ServiceMessageCodec codec) {
    this.codec = codec;
  }

  @Override
  public ClientChannel create(Address address) {
    final Map<Address, Publisher<RSocket>> monoMap = rSockets; // keep reference for threadsafety
    Publisher<RSocket> rSocket = monoMap.computeIfAbsent(address, address1 -> connect(address1, monoMap));
    return new RSocketServiceClientAdapter(rSocket, codec);
  }

  private static Publisher<RSocket> connect(Address address, Map<Address, Publisher<RSocket>> monoMap) {
    return Flux.create((FluxSink<RSocket> sink) -> {
      TcpClient tcpClient =
          TcpClient.create(options -> options.disablePool()
              .host(address.host())
              .port(address.port()));

      TcpClientTransport tcpClientTransport =
          TcpClientTransport.create(tcpClient);

      Mono<RSocket> rSocketMono =
          RSocketFactory.connect().transport(tcpClientTransport).start();

      rSocketMono.subscribe(
          rSocket -> {
            LOGGER.info("Connected successfully on {}", address);
            // setup shutdown hook
            rSocket.onClose()
                .doOnTerminate(() -> {
                  monoMap.remove(address);
                  LOGGER.info("Connection closed on {} and removed from the pool", address);
                  sink.error(new ConnectionErrorException("Connection closed on " + address));
                })
                .subscribe();
            // emit rSocket
            sink.next(rSocket);
          },
          throwable -> {
            LOGGER.warn("Connect failed on {}, cause: {}", address, throwable);
            monoMap.remove(address);
            // emit connection failed error
            sink.error(throwable);
          });
    }).cache();
  }
}
