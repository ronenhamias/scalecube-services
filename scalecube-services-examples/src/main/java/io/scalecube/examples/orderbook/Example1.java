package io.scalecube.examples.orderbook;

import io.scalecube.examples.orderbook.service.FakeMarketDataService;
import io.scalecube.examples.orderbook.service.api.MarketDataService;
import io.scalecube.examples.orderbook.service.engine.Order;
import io.scalecube.services.Microservices;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import reactor.core.publisher.Flux;

public class Example1 {

  static Random rnd = new Random();
  static AtomicInteger orderId = new AtomicInteger(1);

  public static void main(String[] args) throws InterruptedException {

    Microservices ms = Microservices.builder()
        .services(new FakeMarketDataService())
        .build();

    MarketDataService marketService = ms.call().api(MarketDataService.class);

    marketService.asks().subscribe(consumer -> {
      System.out.println(consumer);
    });

    marketService.bids().subscribe(consumer -> {
      System.out.println(consumer);
    });

    // Generate ask and bid orders
    Flux.interval(Duration.ofMillis(300)).subscribe(consumer -> {
      if (rnd.nextInt(2) == 1) {
        marketService.processOrder(
            new Order("bid", "order" + orderId.incrementAndGet(), rnd.nextInt(10), 2));
      } else {
        marketService.processOrder(
            new Order("ask", "order" + orderId.incrementAndGet(), rnd.nextInt(10), 2));
      }

    });

    Thread.currentThread().join();
  }
}
