package io.scalecube.examples.orderbook.service;

import io.scalecube.examples.orderbook.service.api.MarketData;
import io.scalecube.examples.orderbook.service.api.MarketDataService;
import io.scalecube.examples.orderbook.service.engine.Order;
import io.scalecube.examples.orderbook.service.engine.OrdersBookProcessor;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class FakeMarketDataService implements MarketDataService {

  private final OrdersBookProcessor processor;
  private final  EmitterProcessor<Order> bids = EmitterProcessor.<Order>create();
  private final  EmitterProcessor<Order> asks = EmitterProcessor.<Order>create();

  public FakeMarketDataService() {
    this.processor = new OrdersBookProcessor(bids, asks, 4);
  }

  @Override
  public Flux<MarketData> bids() {
    return processor.bids().map(mapper-> new MarketData("bid", mapper.getKey(),mapper.getValue()));
  }

  @Override
  public Flux<MarketData> asks() {
    return processor.asks().map(mapper-> new MarketData("ask",mapper.getKey(),mapper.getValue()));
  }

  @Override
  public Mono<Void> processOrder(Order order) {
    if ("ask".equals(order.type())) {
      asks.onNext(order);
    } else if("bid".equals(order.type())) {
      bids.onNext(order);
    }
    return Mono.empty();
  }

}
