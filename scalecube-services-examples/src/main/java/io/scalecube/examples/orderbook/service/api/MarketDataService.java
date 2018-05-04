package io.scalecube.examples.orderbook.service.api;

import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;

import reactor.core.publisher.Flux;

@Service("io.scalecube.examples.MarketData")
public interface MarketDataService {

  @ServiceMethod("orderBook")
  Flux<MarketData> orderBook(Instrument instrument);
}
