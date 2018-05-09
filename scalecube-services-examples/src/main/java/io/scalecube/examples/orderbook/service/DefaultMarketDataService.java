package io.scalecube.examples.orderbook.service;

import io.scalecube.examples.orderbook.service.api.MarketDataService;
import io.scalecube.examples.orderbook.service.engine.Order;
import io.scalecube.examples.orderbook.service.engine.OrdersBookProcessor;
import io.scalecube.examples.orderbook.service.engine.events.Match;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class DefaultMarketDataService implements MarketDataService {

  private final OrdersBookProcessor processor;
  
  public DefaultMarketDataService() {
    this.processor = new OrdersBookProcessor();
  }


  @Override
  public Mono<Void> processOrder(Order order) {
    processor.onNext(order);
    return Mono.empty();
  }


  @Override
  public Flux<Match> match() {
    return processor.listenMatch();
  }

}
