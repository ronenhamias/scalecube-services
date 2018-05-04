package io.scalecube.examples.orderbook;

import io.scalecube.examples.orderbook.service.engine.Ask;
import io.scalecube.examples.orderbook.service.engine.Bid;
import io.scalecube.examples.orderbook.service.engine.Order;
import io.scalecube.examples.orderbook.service.engine.OrdersBookProcessor;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class Example1 {


  public static void main(String[] args) throws InterruptedException {

    Random rnd = new Random();
    AtomicInteger orderId = new AtomicInteger(1);
    Flux<Order> bids = Flux.interval(Duration.ofMillis(300))
        .subscribeOn(Schedulers.single())
        .map(onNext -> new Bid("order" + orderId.incrementAndGet(), rnd.nextInt(10), 2));
    
    Flux<Order> asks = Flux.interval(Duration.ofMillis(300))
        .map(onNext -> new Ask("order" + orderId.incrementAndGet(), rnd.nextInt(10), 2));

    OrdersBookProcessor processor = new OrdersBookProcessor(bids, asks, 4);

    processor.asks().subscribe(consumer->{
      System.out.println(consumer);
    });
    
    processor.bids().subscribe(consumer->{
      System.out.println(consumer);
    });
     
    Thread.currentThread().join();
  }
}
