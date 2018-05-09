package io.scalecube.examples.orderbook.service.engine;

import io.scalecube.examples.orderbook.service.engine.events.AddOrder;
import io.scalecube.examples.orderbook.service.engine.events.CancelOrder;
import io.scalecube.examples.orderbook.service.engine.events.Match;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

public class OrdersBookProcessor {

  private final EmitterProcessor<Order> inboundOrders = EmitterProcessor.<Order>create();

  public OrdersBookProcessor() {
    inboundOrders.subscribe(order -> {
      orderBook.enter(order.id(), order.level().side(), order.level().price(), order.remainingQuantity());
    });
  }

  OrderBook orderBook = new OrderBook();
  
  /**
   * Cancel a quantity of an order.
   *
   * @param orderId the order identifier
   * @param canceledQuantity the canceled quantity
   * @param remainingQuantity the remaining quantity
   */
  Flux<CancelOrder> listenCancel(){
    return orderBook.cancel();
  }
  
  /**
   * Add an order to the order book.
   *
   * @param orderId the order identifier
   * @param side the side
   * @param price the limit price
   * @param size the size
   */
  public Flux<AddOrder> listenAdd(){
    return orderBook.add();
  }
  
  /**
   * Match an incoming order to a resting order in the order book. The match
   * occurs at the price of the order in the order book.
   *
   * @param restingOrderId the order identifier of the resting order
   * @param incomingOrderId the order identifier of the incoming order
   * @param incomingSide the side of the incoming order
   * @param price the execution price
   * @param executedQuantity the executed quantity
   * @param remainingQuantity the remaining quantity of the resting order
   */
  public Flux<Match> listenMatch(){
    return orderBook.match();
  }
  
  public void onNext(Order order) {
    inboundOrders.onNext(order);
  }

}
