package io.scalecube.examples.orderbook.service.engine;

import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

public class OrdersBookProcessor {


  Map<Integer, Map<Long, Order>> asksBuffer = new ConcurrentSkipListMap<>((Integer v1, Integer v2) -> {
    return deccending(v1, v2);
  });

  Map<Integer, Map<Long, Order>> bidsBuffer = new ConcurrentSkipListMap<>((Integer v1, Integer v2) -> {
    return accending(v1, v2);
  });

  private EmitterProcessor<Entry<Integer, Integer>> askStream = EmitterProcessor.<Entry<Integer, Integer>>create();

  private EmitterProcessor<Entry<Integer, Integer>> bidStream = EmitterProcessor.<Entry<Integer, Integer>>create();

  public Flux<Entry<Integer, Integer>> asks() {
    return askStream;
  }

  public Flux<Entry<Integer, Integer>> bids() {
    return bidStream;
  }

  public OrdersBookProcessor(Flux<Order> bids, Flux<Order> asks, int level) {

    bids.subscribe(onNext -> {
      if (!bidsBuffer.containsKey(onNext.price())) {
        bidsBuffer.put(onNext.price(), new ConcurrentSkipListMap<>((Long v1, Long v2) -> accending(v1, v2)));
      }
      bidsBuffer.get(onNext.price()).put(onNext.time(), onNext);
    });

    asks.subscribe(onNext -> {
      if (!asksBuffer.containsKey(onNext.price())) {
        asksBuffer.put(onNext.price(), new ConcurrentSkipListMap<>((Long v1, Long v2) -> deccending(v1, v2)));
      }
      asksBuffer.get(onNext.price()).put(onNext.time(), onNext);
    });
    emitAsks();
    emitBids();
  }

  private void emitAsks() {

    Flux.interval(Duration.ofSeconds(1))
        .subscribe(consumer -> {
          Set<Entry<Integer, Integer>> items = from(asksBuffer);
          items.forEach(item -> {
            askStream.onNext(item);
          });
        });
  }

  private void emitBids() {
    Flux.interval(Duration.ofSeconds(1))
        .subscribe(consumer -> {
          Set<Entry<Integer, Integer>> items = from(bidsBuffer);
          items.forEach(item -> {
            bidStream.onNext(item);
          });
        });
  }

  private Set<Entry<Integer, Integer>> from(Map<Integer, Map<Long, Order>> asksBuffer) {
    Map<Integer, Integer> result = new ConcurrentSkipListMap<>((Integer v1, Integer v2) -> accending(v1, v2));

    asksBuffer.values().forEach(action -> {
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
      int units = value.values().stream().mapToInt(Order::units).sum();
      int price = value.values().stream().distinct().findFirst().get().price();
      result.put(price, units);
    });
    return result;
  }
  
  private int deccending(Long v1, Long v2) {
    return v1 > v2 ? -1 : v1 < v2 ? +1 : 0;
  }

  private int deccending(Integer v1, Integer v2) {
    return v1 > v2 ? -1 : v1 < v2 ? +1 : 0;
  }
  
  private int accending(Long v1, Long v2) {
    return v1 < v2 ? -1 : v1 > v2 ? +1 : 0;
  }

  private int accending(Integer v1, Integer v2) {
    return v1 < v2 ? -1 : v1 > v2 ? +1 : 0;
  }


}
