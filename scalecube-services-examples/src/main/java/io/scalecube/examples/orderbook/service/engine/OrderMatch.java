package io.scalecube.examples.orderbook.service.engine;

public class OrderMatch {

  @Override
  public String toString() {
    return "OrderMatch [bid=" + bid + ", ask=" + ask + "]";
  }

  private Bid bid;
  private Ask ask;

  public OrderMatch(Bid bid, Ask ask) {
    this.bid = bid;
    this.ask = ask;
  }

  public Bid bid() {
    return bid;
  }
  
  public Ask ask() {
    return ask;
  }
}
