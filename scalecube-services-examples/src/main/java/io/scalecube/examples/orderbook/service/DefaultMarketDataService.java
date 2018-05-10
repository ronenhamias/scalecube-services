package io.scalecube.examples.orderbook.service;

import io.scalecube.examples.orderbook.service.api.MarketDataService;
import io.scalecube.examples.orderbook.service.engine.Order;
import io.scalecube.examples.orderbook.service.engine.OrderBookSnapshoot;
import io.scalecube.examples.orderbook.service.engine.OrderBooks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class DefaultMarketDataService implements MarketDataService {

  List<String> instumentList = new ArrayList<>();

  OrderBooks books;
  AtomicLong lastTrade = new AtomicLong();

  public DefaultMarketDataService() {
    instumentList.add("ORCL");
    books = new OrderBooks(instumentList);
  }


  @Override
  public Mono<Void> processOrder(Order order) {
    books.enterOrder(order, "ORCL");
    return Mono.empty();
  }

  @Override
  public Flux<OrderBookSnapshoot> orderBook() {
    
    return Flux.interval(Duration.ofSeconds(1))
        .map(mapper -> new OrderBookSnapshoot(books.book("ORCL")));
  }

}
