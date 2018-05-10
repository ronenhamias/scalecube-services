package io.scalecube.examples.orderbook;

import io.scalecube.examples.orderbook.service.DefaultMarketDataService;
import io.scalecube.examples.orderbook.service.api.MarketDataService;
import io.scalecube.examples.orderbook.service.engine.Order;
import io.scalecube.examples.orderbook.service.engine.OrderBook;
import io.scalecube.examples.orderbook.service.engine.OrderBookSnapshoot;
import io.scalecube.examples.orderbook.service.engine.PriceLevel;
import io.scalecube.examples.orderbook.service.engine.Side;
import io.scalecube.services.Microservices;

import java.time.Duration;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import reactor.core.publisher.Flux;

public class Example1 {

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
    
    // Generate ask and bid orders
    Flux.interval(Duration.ofMillis(10)).subscribe(consumer -> {
      if (rnd.nextInt(2) == 1) {
        marketService.processOrder(new Order(
            new PriceLevel(Side.BUY, rnd.nextInt(10)+1),
            orderId.incrementAndGet(),
            Long.valueOf(rnd.nextInt(10)+1 + "")));
      } else {
        marketService.processOrder(new Order(
            new PriceLevel(Side.SELL, rnd.nextInt(10)+1),
            orderId.incrementAndGet(),
            Long.valueOf(rnd.nextInt(10)+1 + "")));
      }
    });


    Thread.currentThread().join();
  }


  private static void print(OrderBookSnapshoot snapshoot) {
    OrderBook book = snapshoot.book();
    Set asks = book.getAskPrices();
    Set bids = book.getBidPrices();
    System.out.println("====== Asks ========");
    System.out.println("  Price\t|  Amount");
    asks.forEach(price -> {
      System.out.println("   " + price + "\t|    " + book.getAskSize((long)  price));
    });
    
    System.out.println("====================\nCurrent Price (" + book.getBestBidPrice()+ ")");
    System.out.println("====== Bids ========");
    System.out.println("  Price\t|  Amount");
    bids.forEach(price -> {
      System.out.println("   " + price + "\t|    " + book.getAskSize((long)  price));
    });
  }

}
