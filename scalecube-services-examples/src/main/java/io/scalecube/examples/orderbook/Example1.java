package io.scalecube.examples.orderbook;

import io.scalecube.examples.orderbook.service.FakeMarketDataService;
import io.scalecube.examples.orderbook.service.api.MarketDataService;
import io.scalecube.examples.orderbook.service.engine.Order;
import io.scalecube.services.Microservices;

import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import reactor.core.publisher.Flux;

public class Example1 {

  static Random rnd = new Random(5);
  static AtomicInteger orderId = new AtomicInteger(1);
  static Map<Integer,Integer> bids = new ConcurrentSkipListMap<Integer,Integer>((Integer v1, Integer v2) -> accending(v1, v2));
  static Map<Integer,Integer> asks = new ConcurrentSkipListMap<Integer,Integer>((Integer v1, Integer v2) -> deccending(v1, v2));
  
  public static void main(String[] args) throws InterruptedException {


    Microservices ms = Microservices.builder()
        .services(new FakeMarketDataService())
        .build();

    MarketDataService marketService = ms.call().api(MarketDataService.class);

    marketService.asks().subscribe(item -> {
      asks.put(item.price(), item.amount());
      print();
    });

    marketService.bids().subscribe(item -> {
      bids.put(item.price(), item.amount());
      print();
    });

    // Generate ask and bid orders
    Flux.interval(Duration.ofMillis(100)).subscribe(consumer -> {
      if (rnd.nextInt(2) == 1) {
        marketService.processOrder(
            new Order("bid", "order" + orderId.incrementAndGet(), rnd.nextInt(10)+10, 2));
      } else {
        marketService.processOrder(
            new Order("ask", "order" + orderId.incrementAndGet(), rnd.nextInt(10)+10, 2));
      }

    });

    Thread.currentThread().join();
  }
  

  private static void print() {
    System.out.println("====== Asks ========");
    System.out.println("  Price\t|\tAmount");
    asks.entrySet().forEach(action->{
      System.out.println("  "+action.getKey() + "\t|\t" +  action.getValue());   
    });
    System.out.println("====== Bids ========");
    System.out.println("  Price\t|\tAmount");
    bids.entrySet().forEach(action->{
      System.out.println("  "+action.getKey() + "\t|\t" +  action.getValue());   
    });
  }


  private static int deccending(Integer v1, Integer v2) {
    return v1 > v2 ? -1 : v1 < v2 ? +1 : 0;
  }
  

  private static int accending(Integer v1, Integer v2) {
    return v1 < v2 ? -1 : v1 > v2 ? +1 : 0;
  }
}
