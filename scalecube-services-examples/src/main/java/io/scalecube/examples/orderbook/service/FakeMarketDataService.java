package io.scalecube.examples.orderbook.service;

import io.scalecube.examples.orderbook.service.api.MarketDataService;
import io.scalecube.examples.orderbook.service.engine.Order;
import io.scalecube.examples.orderbook.service.engine.OrdersBookProcessor;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class FakeMarketDataService implements MarketDataService {

  OrdersBookProcessor processor;
  private EmitterProcessor<Order> bids = EmitterProcessor.<Order>create();
  private EmitterProcessor<Order> asks = EmitterProcessor.<Order>create();

  public FakeMarketDataService() {
    processor = new OrdersBookProcessor(bids, asks, 4);
    processor.asks().subscribe(consumer -> {
      System.out.println(consumer);
    });

    processor.bids().subscribe(consumer -> {
      System.out.println(consumer);
    });
  }


  @Override
  public Flux<Order> bids() {
    return bids;
  }

  @Override
  public Flux<Order> asks() {
    return asks;
  }


  @Override
  public Mono<Void> processOrder(Order order) {
    if ("ask".equals(order.type())) {
      asks.onNext(order);
    } else
      bids.onNext(order);
    return Mono.empty();
  }

}
