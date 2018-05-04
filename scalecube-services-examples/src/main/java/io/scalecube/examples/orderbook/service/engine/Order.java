package io.scalecube.examples.orderbook.service.engine;

public class Order {
  @Override
  public String toString() {
    return "Bid [orderId=" + orderId + ", price=" + price + ", size=" + size + "]";
  }

  private String orderId;
  private int price;
  private int size;
  private Long time;
  private String type;

  public Order(String type, String orderId, int price, int size) {
    this.orderId = orderId;
    this.price = price;
    this.size = size;
    this.type = type;
    this.time = System.currentTimeMillis();
  }

  public String getOrderId() {
    return orderId;
  }

  public int getPrice() {
    return price;
  }

  public int getUnits() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
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

  public String type() {
    return this.type;
  }


}
