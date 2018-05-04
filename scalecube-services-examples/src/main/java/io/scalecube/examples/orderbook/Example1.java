package io.scalecube.examples.orderbook;

import io.scalecube.examples.orderbook.service.engine.Order;

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
        .map(onNext -> new Order("bid","order" + orderId.incrementAndGet(), rnd.nextInt(10), 2));
    
    Flux<Order> asks = Flux.interval(Duration.ofMillis(300))
        .map(onNext -> new Order("ask","order" + orderId.incrementAndGet(), rnd.nextInt(10), 2));

     
    Thread.currentThread().join();
  }
}
