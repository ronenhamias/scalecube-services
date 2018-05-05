package io.scalecube.examples.orderbook.service.engine;

public class Order {


  private String orderId;
  private int price;
  private int size;
  private Long time;
  private String type;

  public Order() {}

  public Order(String type, String orderId, int price, int size) {
    this.orderId = orderId;
    this.price = price;
    this.size = size;
    this.type = type;
    this.time = System.currentTimeMillis();
  }

  public String type() {
    return this.type;
  }

  public String orderId() {
    return orderId;
  }

  public int price() {
    return price;
  }

  public int units() {
    return size;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Order order = (Order) o;

    return orderId.equals(order.orderId);

  }

  @Override
  public int hashCode() {
    return orderId.hashCode();
  }

  public Long time() {
    return time;
  }

  @Override
  public String toString() {
    return "Order [type=" + type + ", orderId=" + orderId + ", price=" + price + ", size=" + size +", time=" + time +"]";
  }


}
