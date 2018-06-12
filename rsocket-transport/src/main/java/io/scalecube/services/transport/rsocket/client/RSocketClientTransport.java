package io.scalecube.services.transport.rsocket.client;

import io.scalecube.services.codec.ServiceMessageCodec;
import io.scalecube.services.transport.client.api.ClientChannel;
import io.scalecube.services.transport.client.api.ClientTransport;
import io.scalecube.transport.Address;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.tcp.TcpClient;

public class RSocketClientTransport implements ClientTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(RSocketClientTransport.class);

  private final Map<Address, Publisher<RSocket>> rSockets = new ConcurrentHashMap<>();

  private final ServiceMessageCodec codec;

  public RSocketClientTransport(ServiceMessageCodec codec) {
    this.codec = codec;
  }

  public static void main(String[] args) throws InterruptedException {
    Publisher<Object> processor = Flux.create(sink -> {
      CompletableFuture<Void> promise = CompletableFuture.runAsync(() -> {
        try {
          Thread.sleep(1000);

          sink.next(1); // rSocket succesful connection

          Thread.sleep(2000);

          sink.error(new RuntimeException("connection down"));

        } catch (Exception e) {
          e.printStackTrace();
        }
      }, Executors.newFixedThreadPool(1));
    })
        .cache(0);

    Flux.from(processor)
        .doOnEach(signal -> System.out.println("1, o = " + signal))
        .flatMap(o -> Flux.interval(Duration.ofSeconds(1)))
        .subscribe(o -> System.out.println("1, o = " + o), t -> System.err.println("1, " + t),
            () -> System.out.println("1, comleted"));

    Flux.from(processor)
        .doOnEach(signal -> System.out.println("2, o = " + signal))
        .flatMap(o -> Flux.interval(Duration.ofSeconds(1)))
        .subscribe(o -> System.out.println("2, o = " + o), t -> System.err.println("2, " + t),
            () -> System.out.println("2, comleted"));

    Mono.from(processor)
        .doOnEach(signal -> System.out.println("4, o = " + signal))
        .flatMapMany(o -> Flux.interval(Duration.ofSeconds(1)))
        .subscribe(o -> System.out.println("4, o = " + o), t -> System.err.println("4, " + t),
            () -> System.out.println("4, comleted"));

    Thread.sleep(5000);

    Flux.from(processor)
        .doOnEach(signal -> System.out.println("3, o = " + signal))
        .flatMap(o -> Flux.interval(Duration.ofSeconds(1)))
        .subscribe(o -> System.out.println("3, o = " + o), t -> System.err.println("3, " + t),
            () -> System.out.println("3, comleted"));

    Mono.from(processor)
        .doOnEach(signal -> System.out.println("5, o = " + signal))
        .flatMapMany(o -> Flux.interval(Duration.ofSeconds(1)))
        .subscribe(o -> System.out.println("5, o = " + o), t -> System.err.println("5, " + t),
            () -> System.out.println("5, comleted"));

    Thread.currentThread().join();
  }

  @Override
  public ClientChannel create(Address address) {
    final Map<Address, Publisher<RSocket>> monoMap = rSockets; // keep reference for threadsafety
    Publisher<RSocket> rSocket = monoMap.computeIfAbsent(address, address1 -> connect(address1, monoMap));
    return new RSocketServiceClientAdapter(rSocket, codec);
  }

  private static Publisher<RSocket> connect(Address address, Map<Address, Publisher<RSocket>> monoMap) {
    EmitterProcessor<RSocket> rSocketProcessor = EmitterProcessor.create(1);

    RSocketFactory.connect()
        .transport(TcpClientTransport.create(
            TcpClient.create(options -> options
                .disablePool()
                .host(address.host())
                .port(address.port())
                .afterNettyContextInit(nettyContext -> {
                  // add handler to react on remote node closes connection
                  nettyContext.addHandler(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) {
                      monoMap.remove(address);
                      LOGGER.info("Connection became inactive on {} and removed from the pool", address);
                      rSocketProcessor.onError(new RuntimeException());
                      ctx.fireChannelInactive();
                    }
                  });
                }))))
        .start()
        .subscribe(
            rSocket -> {
              LOGGER.info("Connected successfully on {}", address);
              rSocket.onClose().subscribe(aVoid -> {
                monoMap.remove(address);
                LOGGER.info("Connection closed on {} and removed from the pool", address);
                rSocketProcessor.onError(new RuntimeException());
              });
              rSocketProcessor.onNext(rSocket);
            },
            throwable -> {
              monoMap.remove(address);
              LOGGER.warn("Connect failed on {}, cause: {}", address, throwable);
              rSocketProcessor.onError(throwable);
            },
            () -> {
              monoMap.remove(address);
              LOGGER.info("Connection became inactive on {} due onComplete and removed from the pool", address);
              rSocketProcessor.onError(new RuntimeException());
            });

    return rSocketProcessor;
  }
}
