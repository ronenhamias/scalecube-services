package io.scalecube.examples.orderbook.service.engine.events;

import io.scalecube.examples.orderbook.service.engine.Side;

public class AddOrder {

  long orderId;
  Side buy;
  long price;
  long remainingQuantity;

  public AddOrder() {};
  public AddOrder(long orderId, Side buy, long price, long remainingQuantity) {
    this.orderId = orderId;
    this.buy = buy;
    this.price = price;
    this.remainingQuantity = remainingQuantity;
  }

  public long orderId() {
    return orderId;
  }

  public Side buy() {
    return buy;
  }

  public long price() {
    return price;
  }

  public long quantity() {
    return remainingQuantity;
  }

}
