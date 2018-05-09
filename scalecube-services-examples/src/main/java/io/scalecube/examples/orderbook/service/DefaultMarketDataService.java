package io.scalecube.examples.orderbook.service;

import io.scalecube.examples.orderbook.service.api.MarketDataService;
import io.scalecube.examples.orderbook.service.engine.Order;
import io.scalecube.examples.orderbook.service.engine.OrderBookSnapshoot;
import io.scalecube.examples.orderbook.service.engine.OrdersBookProcessor;
import io.scalecube.examples.orderbook.service.engine.Side;
import io.scalecube.examples.orderbook.service.engine.events.Match;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class DefaultMarketDataService implements MarketDataService {

  Map<Long, Long> bids =
      new ConcurrentSkipListMap<Long, Long>((Long v1, Long v2) -> accending(v1, v2));

  Map<Long, Long> asks =
      new ConcurrentSkipListMap<Long, Long>((Long v1, Long v2) -> deccending(v1, v2));

  private final OrdersBookProcessor processor;

  AtomicLong lastTrade = new AtomicLong(); 
  public DefaultMarketDataService() {
    this.processor = new OrdersBookProcessor();
    
    processor.listenMatch().subscribe(consumer->{
      lastTrade.set(consumer.price());
    });
    
    processor.listenAdd().subscribe(order -> {
      if (order.side().equals(Side.BUY)) {
        Long currentQuantity = asks.get(order.price());
        if (currentQuantity != null) {
          asks.put(order.price(), currentQuantity + order.quantity());
        } else {
          asks.put(order.price(), order.quantity());
        }
      } else {
        Long currentQuantity = bids.get(order.price());
        if (currentQuantity != null) {
          bids.put(order.price(), currentQuantity + order.quantity());
        } else {
          bids.put(order.price(), order.quantity());
        }
      }
    });

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


  @Override
  public Flux<OrderBookSnapshoot> orderBook() {
    return Flux.interval(Duration.ofSeconds(1))
        .map(mapper-> new OrderBookSnapshoot(bids,asks,lastTrade.get()));
  }



  private static int deccending(Long v1, Long v2) {
    return v1 > v2 ? -1 : v1 < v2 ? +1 : 0;
  }

  private static int accending(Long v1, Long v2) {
    return v1 < v2 ? -1 : v1 > v2 ? +1 : 0;
  }
}
