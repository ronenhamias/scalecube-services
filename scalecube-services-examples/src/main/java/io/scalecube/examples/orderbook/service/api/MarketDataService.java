package io.scalecube.examples.orderbook.service.api;

import io.scalecube.examples.orderbook.service.engine.Order;
import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service("io.scalecube.examples.MarketData")
public interface MarketDataService {

  @ServiceMethod("processOrder")
  Mono<Void> processOrder(Order order);

  @ServiceMethod("bids")
  Flux<MarketData> bids();

  @ServiceMethod("asks")
  Flux<MarketData> asks();
}
