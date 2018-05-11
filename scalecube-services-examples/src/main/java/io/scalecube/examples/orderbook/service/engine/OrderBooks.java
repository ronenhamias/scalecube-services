package io.scalecube.examples.orderbook.service.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class OrderBooks {

  private Map<String, MatchingEngine> engines = new HashMap();
  private Map<String, OrderBook> books = new HashMap();
  private Long2ObjectOpenHashMap<Order> orders;

  public OrderBooks(List<String> instruments) {
    this.orders = new Long2ObjectOpenHashMap<>();

    for (String instrument : instruments) {
      OrderBook book = new OrderBook(instrument);
      books.put(instrument, book);
      MatchingEngine engine = new MatchingEngine();
      engines.put(instrument, engine);

      engine.add().subscribe(order -> {
        book.add(order);
      });
      
      engine.match().subscribe(order -> {
        book.update(order.incomingSide(), order.price(), order.executedQuantity());
      });
      
    }
  }
  public OrderBook book(String instrument) {
    return books.get(instrument);
  }
  
  public void enterOrder(Order order, String instrument) {
    engines.get(instrument).enter(order);
  }

  public void cancel(Order order) {
    orders.remove(order.id());
  }

}
