package io.scalecube.services.benchmarks;

import io.netty.channel.Channel;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.IntStream;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.ipc.netty.tcp.TcpClient;
import reactor.ipc.netty.tcp.TcpServer;

public class BugRunner {

  private static final ThreadLocal<Map<InetSocketAddress, Mono<RSocket>>> rSockets =
      ThreadLocal.withInitial(ConcurrentHashMap::new);

  public static void main(String[] args) throws InterruptedException {

    Scheduler scheduler = createScheduler();

    TcpServer tcpServer = createTcpServer();

    InetSocketAddress address = handleRequestStream(tcpServer, (Payload payload) -> {
      String metadataUtf8 = payload.getMetadataUtf8();
      String dataUtf8 = payload.getDataUtf8();
      return Flux.fromStream(
          IntStream.range(0, (int) 1e6)
              .mapToObj(i1 -> {
                return new String[] {"echo-" + dataUtf8 + i1, "echo-" + metadataUtf8 + i1};
              })
              .map(arr -> DefaultPayload.create(arr[0], arr[1])));
    });

    System.out.println("Listen on serverAddress: " + address);

    Flux.range(0, 1000)
        .parallel()
        .runOn(Schedulers.parallel())
        .flatMap(i -> {
          Payload payload = DefaultPayload.create("data-" + i, "metadata-" + i);

          Map<InetSocketAddress, Mono<RSocket>> monoMap = rSockets.get();
          Mono<RSocket> rSocketMono =
              monoMap.computeIfAbsent(address, BugRunner::getOrCreateRSocket);

          return rSocketMono
              .flatMapMany(rSocket -> rSocket.requestStream(payload))
              .doOnNext(next -> {
                String metadataUtf8 = next.sliceMetadata().toString(Charset.defaultCharset());
                String dataUtf8 = next.sliceData().toString(Charset.defaultCharset());
                if (!dataUtf8.startsWith("echo-data-") || !metadataUtf8.startsWith("echo-metadata-")) {
                  System.err.println("!!! ERROR OCCURED data:|" + dataUtf8 + "|metadata:" + metadataUtf8);
                  System.exit(42);
                }
              });
        }).doOnComplete(() -> {
          System.out.println("done");
          System.exit(0);
        }).subscribe();

    // Flux.merge(Flux.fromStream(LongStream.range(0, Long.MAX_VALUE).boxed())
    // .publishOn(scheduler)
    // .map(i -> {
    // Payload payload = DefaultPayload.create("data-" + i, "metadata-" + i);
    //
    // Map<InetSocketAddress, Mono<RSocket>> monoMap = rSockets.get();
    // Mono<RSocket> rSocketMono =
    // monoMap.computeIfAbsent(address, BugRunner::getOrCreateRSocket);
    //
    // return rSocketMono
    // .flatMapMany(rSocket -> rSocket.requestStream(payload))
    // .doOnNext(next -> {
    // String dataUtf8 = next.sliceData().toString(Charset.defaultCharset());
    // String metadataUtf8 = next.sliceMetadata().toString(Charset.defaultCharset());
    // if (!dataUtf8.startsWith("echo-data-") || !metadataUtf8.startsWith("echo-metadata-")) {
    // System.err.println("!!! ERROR OCCURED data:|" + dataUtf8 + "|metadata:" + metadataUtf8);
    // System.exit(42);
    // }
    // });
    // }))
    // .take(Duration.ofSeconds(60))
    // .blockLast();

    Thread.currentThread().join();
  }

  public static Mono<RSocket> getOrCreateRSocket(InetSocketAddress address) {
    TcpClient tcpClient = TcpClient.create(opts -> opts.connectAddress(() -> address));
    return RSocketFactory.connect().transport(TcpClientTransport.create(tcpClient)).start().cache();
  }

  public static InetSocketAddress handleRequestStream(TcpServer tcpServer,
      Function<Payload, Flux<Payload>> requestStreamFunction) {
    return RSocketFactory.receive()
        .acceptor((setup, rSocket) -> {
          System.out.println("rSocket connection setup: " + setup.toString());
          return Mono.just(new AbstractRSocket() {
            @Override
            public Flux<Payload> requestStream(Payload payload) {
              return requestStreamFunction.apply(payload);
            }
          });
        })
        .transport(TcpServerTransport.create(tcpServer))
        .start()
        .block()
        .address();
  }

  public static TcpServer createTcpServer() {
    InetSocketAddress address = new InetSocketAddress(0);
    return TcpServer.create(options -> options.listenAddress(address)
        .afterNettyContextInit(nettyContext -> {
          Channel channel = nettyContext.channel();
          System.out.println("Accepted tcp: " + channel);
        }));
  }

  public static Scheduler createScheduler() {
    int nThreads = Runtime.getRuntime().availableProcessors();
    ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
    return Schedulers.fromExecutor(executorService);
  }
}
