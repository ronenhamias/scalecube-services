package io.scalecube.examples.orderbook.service.engine;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class OrdersBookProcessor {

  private Flux<OrderMatch> matched;

  Map<Integer, Map<Long, Order>> asksBuffer = new ConcurrentSkipListMap<>((Integer v1, Integer v2) -> {
    return deccending(v1, v2);
  });

  Map<Integer, Map<Long, Order>> bidsBuffer = new ConcurrentSkipListMap<>((Integer v1, Integer v2) -> {
    return accending(v1, v2);
  });

  public OrdersBookProcessor(Flux<Order> bids, Flux<Order> asks, int level) {

    bids.subscribe(onNext -> {
      if (!bidsBuffer.containsKey(onNext.getPrice())) {
        bidsBuffer.put(onNext.getPrice(), new ConcurrentSkipListMap<>((Long v1, Long v2) -> accending(v1, v2)));
      }
      bidsBuffer.get(onNext.getPrice()).put(onNext.time(), onNext);
    });

    asks.subscribe(onNext -> {
      if (!asksBuffer.containsKey(onNext.getPrice())) {
        asksBuffer.put(onNext.getPrice(), new ConcurrentSkipListMap<>((Long v1, Long v2) -> deccending(v1, v2)));
      }
      asksBuffer.get(onNext.getPrice()).put(onNext.time(), onNext);
    });
  }

  public Flux<Entry<Integer, Integer>> asks() {
    return collectStream(from(asksBuffer.values()));
  }

  public Flux<Entry<Integer, Integer>> bids() {
    return collectStream(from(bidsBuffer.values()));
  }

  private Set<Entry<Integer, Integer>> from(Collection<Map<Long, Order>> collection) {
    Map<Integer, Integer> result = new ConcurrentSkipListMap<>((Integer v1, Integer v2) -> accending(v1, v2));

    collection.forEach(action -> {
      Map<Integer, Integer> aggregate = sum(action);
      aggregate.entrySet().forEach(item -> {
        result.put(item.getKey(), item.getValue());
      });
    });
    return result.entrySet();

  }



  private Map<Integer, Integer> sum(Map<Long, Order> value) {
    Map<Integer, Integer> result = new ConcurrentSkipListMap<>((Integer v1, Integer v2) -> accending(v1, v2));

    value.entrySet().forEach(action -> {
      int units = value.values().stream().mapToInt(Order::getUnits).sum();
      int price = value.values().stream().distinct().findFirst().get().getPrice();
      result.put(price, units);
    });
    return result;
  }

  private Flux<Entry<Integer, Integer>> collectStream(Set<Entry<Integer, Integer>> askOrBids) {
    EmitterProcessor<Entry<Integer, Integer>> stream = EmitterProcessor.<Entry<Integer, Integer>>create();
    Flux.interval(Duration.ofSeconds(1))
        .subscribeOn(Schedulers.single())
        .subscribe(s -> {
          askOrBids.stream().forEach(action -> {
            stream.onNext(action);
          });
        });
    return stream;
  }



  public Flux<OrderMatch> matched() {
    return matched;
  }

  private int deccending(Long v1, Long v2) {
    return v1 > v2 ? -1 : v1 < v2 ? +1 : 0;
  }

  private int accending(Long v1, Long v2) {
    return v1 < v2 ? -1 : v1 > v2 ? +1 : 0;
  }

  private int deccending(Integer v1, Integer v2) {
    return v1 > v2 ? -1 : v1 < v2 ? +1 : 0;
  }

  private int accending(Integer v1, Integer v2) {
    return v1 < v2 ? -1 : v1 > v2 ? +1 : 0;
  }
}
