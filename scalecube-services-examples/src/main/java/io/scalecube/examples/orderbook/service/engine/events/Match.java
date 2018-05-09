package io.scalecube.examples.orderbook.service.engine.events;

import io.scalecube.examples.orderbook.service.engine.Side;

public class Match {



  long restingOrderId;
  long incomingOrderId;
  Side incomingSide;
  long price;
  long executedQuantity;
  long remainingQuantity;

  public Match() {};
  
  public Match(long restingOrderId,
      long incomingOrderId,
      Side incomingSide,
      long price,
      long executedQuantity,
      long remainingQuantity) {

    this.restingOrderId = restingOrderId;
    this.incomingOrderId = incomingOrderId;
    this.incomingSide = incomingSide;
    this.price = price;
    this.executedQuantity = executedQuantity;
    this.remainingQuantity = remainingQuantity;
  }

  public long restingOrderId() {
    return restingOrderId;
  }

  public long incomingOrderId() {
    return incomingOrderId;
  }

  public Side incomingSide() {
    return incomingSide;
  }

  public long price() {
    return price;
  }

  public long executedQuantity() {
    return executedQuantity;
  }

  public long remainingQuantity() {
    return remainingQuantity;
  }

  @Override
  public String toString() {
    return "Match [restingOrderId=" + restingOrderId + ", incomingOrderId=" + incomingOrderId + ", incomingSide="
        + incomingSide + ", price=" + price + ", executedQuantity=" + executedQuantity + ", remainingQuantity="
        + remainingQuantity + "]";
  }

}
