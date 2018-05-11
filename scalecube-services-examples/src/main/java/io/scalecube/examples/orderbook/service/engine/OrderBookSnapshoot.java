package io.scalecube.examples.orderbook.service.engine;

import java.util.HashMap;
import java.util.Map;

public class OrderBookSnapshoot {

  Map<Long, Long> bids = new HashMap<Long, Long>();

  Map<Long, Long> asks = new HashMap<Long, Long>();

  Long currentPrice;

  private OrderBook book;

  public OrderBookSnapshoot() {

  }

  public OrderBookSnapshoot(Map<Long, Long> bids, Map<Long, Long> asks, Long currentPrice) {
    this.bids = bids;
    this.asks = asks;
    this.currentPrice = currentPrice;
  }

  public OrderBookSnapshoot(OrderBook book) {
    this.book = book;
  }

  public OrderBook book() {
    return book;
  }

  public Map<Long, Long> bids() {
    return bids;
  }

  public Map<Long, Long> asks() {
    return asks;
  }

  public Long currentPrice() {
    return currentPrice;
  }

  @Override
  public String toString() {
    return "OrderBookSnapshoot [book=" + book + "]";
  }
}
