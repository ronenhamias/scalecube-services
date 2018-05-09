package io.scalecube.examples.orderbook;

import io.scalecube.examples.orderbook.service.DefaultMarketDataService;
import io.scalecube.examples.orderbook.service.api.MarketDataService;
import io.scalecube.examples.orderbook.service.engine.Order;
import io.scalecube.examples.orderbook.service.engine.PriceLevel;
import io.scalecube.examples.orderbook.service.engine.Side;
import io.scalecube.services.Microservices;

import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import reactor.core.publisher.Flux;

public class Example1 {

  static Random rnd = new Random(5);
  static AtomicLong orderId = new AtomicLong(1);

  static Map<Integer, Integer> bids =
      new ConcurrentSkipListMap<Integer, Integer>((Integer v1, Integer v2) -> accending(v1, v2));
  static Map<Integer, Integer> asks =
      new ConcurrentSkipListMap<Integer, Integer>((Integer v1, Integer v2) -> deccending(v1, v2));

  public static void main(String[] args) throws InterruptedException {

    Microservices gateway = Microservices.builder().build();

    Microservices ms = Microservices.builder()
        .seeds(gateway.cluster().address())
        .services(new DefaultMarketDataService())
        .build();

    MarketDataService marketService = ms.call().api(MarketDataService.class);

    marketService.match().subscribe(match -> {
      System.out.println(match);
    });
    // Generate ask and bid orders
    Flux.interval(Duration.ofMillis(50)).subscribe(consumer -> {
      if (rnd.nextInt(2) == 1) {
        marketService.processOrder(new Order(
            new PriceLevel(Side.BUY, rnd.nextInt(10) + 1),
            orderId.incrementAndGet(),
            Long.valueOf(rnd.nextInt(100) + "")));
      } else {
        marketService.processOrder(new Order(
            new PriceLevel(Side.SELL, rnd.nextInt(10) + 1),
            orderId.incrementAndGet(),
            Long.valueOf(rnd.nextInt(100) + "")));
      }
    });


    Thread.currentThread().join();
  }


  private static void print() {
    System.out.println("====== Asks ========");
    System.out.println("  Price\t|  Amount");
    asks.entrySet().forEach(action -> {
      System.out.println("   " + action.getKey() + "\t|    " + action.getValue());
    });
    System.out.println("====== Bids ========");
    System.out.println("  Price\t|  Amount");
    bids.entrySet().forEach(action -> {
      System.out.println("   " + action.getKey() + "\t|    " + action.getValue());
    });
  }


  private static int deccending(Integer v1, Integer v2) {
    return v1 > v2 ? -1 : v1 < v2 ? +1 : 0;
  }


  private static int accending(Integer v1, Integer v2) {
    return v1 < v2 ? -1 : v1 > v2 ? +1 : 0;
  }
}
