package io.scalecube.examples.orderbook;

import io.scalecube.examples.orderbook.service.DefaultMarketDataService;
import io.scalecube.examples.orderbook.service.OrderBookSnapshoot;
import io.scalecube.examples.orderbook.service.OrderRequest;
import io.scalecube.examples.orderbook.service.api.MarketDataService;
import io.scalecube.examples.orderbook.service.engine.Order;
import io.scalecube.examples.orderbook.service.engine.OrderBook;
import io.scalecube.examples.orderbook.service.engine.PriceLevel;
import io.scalecube.examples.orderbook.service.engine.events.Side;
import io.scalecube.services.Microservices;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Example1 {

  private static final String INSTRUMENT = "ORCL";

  static Random rnd = new Random(5);
  static AtomicLong orderId = new AtomicLong(1);

  public static void main(String[] args) throws InterruptedException {

    Microservices gateway = Microservices.builder().build();

    Microservices ms = Microservices.builder()
        .seeds(gateway.cluster().address())
        .services(new DefaultMarketDataService())
        .build();

    MarketDataService marketService = ms.call().api(MarketDataService.class);

    marketService.orderBook().subscribe(order -> {
      print(order);
    });

    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(()->{
      try {
        
        if (rnd.nextInt(2) == 1) {
          marketService.processOrder(new OrderRequest(new Order(
              new PriceLevel(Side.BUY, rnd.nextInt(10) + 1), // prices
              System.currentTimeMillis(),
              Long.valueOf(rnd.nextInt(110) + 1 + "")), //units
              INSTRUMENT)).block();
        } else {
          marketService.processOrder(new OrderRequest(new Order(
              new PriceLevel(Side.SELL, rnd.nextInt(10) + 1), // prices
              System.currentTimeMillis(),
              Long.valueOf(rnd.nextInt(70) + 1 + "")), //units
              INSTRUMENT)).block();
        }
      } catch (Throwable ex) {
        ex.printStackTrace();
      }   
    }, 3, 3, TimeUnit.MILLISECONDS);
   

    Thread.currentThread().join();
  }


  private static void print(OrderBookSnapshoot snapshoot) {

    System.out.println("====== Asks ========");
    System.out.println("  Price\t|  Amount");
    SortedMap<Long,Long> orderlist = new TreeMap<Long,Long>(Collections.reverseOrder());
    orderlist.putAll(snapshoot.asks());
    orderlist.entrySet().forEach(entry -> {
      System.out.println("   " + entry.getKey() + "\t|    " +entry.getValue());
    });

    System.out.println("====================\nCurrent Price (" + snapshoot.currentPrice() + ")");
    System.out.println("====== Bids ========");
    System.out.println("  Price\t|  Amount");
    snapshoot.bids().entrySet().forEach(entry -> {
      System.out.println("   " + entry.getKey() + "\t|    " + entry.getValue());
    });
  }

}
