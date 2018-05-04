package io.scalecube.examples.orderbook.service;

import io.scalecube.examples.orderbook.service.api.Instrument;
import io.scalecube.examples.orderbook.service.api.MarketData;
import io.scalecube.examples.orderbook.service.api.MarketDataService;
import io.scalecube.services.annotations.Service;

import reactor.core.publisher.Flux;


public class FakeMarketDataService implements MarketDataService {

  @Override
  public Flux<MarketData> orderBook(Instrument instrument) {
    
    return null;
  }


}
