package io.scalecube.services.transport.rsocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.NetUtil;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.ByteBufPayload;
import io.rsocket.util.DefaultPayload;

import java.io.IOException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.tcp.TcpClient;

public class RSocketClient {
  public static void main(String[] args) throws InterruptedException {
    Mono<RSocket> rsocketMono = RSocketFactory.connect()
        .transport(TcpClientTransport.create(
            TcpClient.create(options -> options
                .disablePool()
                .host(NetUtil.LOCALHOST.getHostAddress())
                .port(4001)
                .afterNettyContextInit(nettyContext -> {
                  nettyContext.addHandler(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) {
                      System.err.println("channelInactive");
                      ctx.fireChannelInactive();
                    }

                    @Override
                    public void channelUnregistered(ChannelHandlerContext ctx) {
                      System.err.println("channelUnregistered");
                      ctx.fireChannelUnregistered();
                    }
                  });
                }))))
        .start();

    Flux<RSocket> connectionFlux = Flux.create((FluxSink<RSocket> sink) -> {
      rsocketMono.subscribe(rSocket -> {
        rSocket.onClose()
            .log("ON_CLOSE: ")
            .doOnTerminate(() -> sink.error(new IOException("Disconnected"))).subscribe();
        sink.next(rSocket);
      }, sink::error/* , sink::complete */);
    }).log("CONNECTION_FLUX");

    connectionFlux.flatMap(rsocket -> rsocket.requestResponse(DefaultPayload.create("hola"))
        .log("REQ_RESP: "))
        .doOnCancel(() -> System.err.println("doOnCancel"))
        .doOnTerminate(() -> System.err.println("doOnTerminate"))
        .single()
        .subscribe();

    connectionFlux.flatMap(rsocket -> rsocket.requestStream(DefaultPayload.create("hola"))
        .log("REQ_STREAM: "))
        .doOnCancel(() -> System.err.println("doOnCancel"))
        .doOnTerminate(() -> System.err.println("doOnTerminate"))
        .map(payload -> ByteBufPayload.create(payload).getDataUtf8())
        .subscribe(System.out::println, Throwable::printStackTrace);


    Thread.currentThread().join();
  }
}
