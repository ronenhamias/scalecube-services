package io.scalecube.examples.orderbook.service.engine;

import java.math.BigDecimal;

public class Ask implements Order{
  
    @Override
  public String toString() {
    return "Ask [orderId=" + orderId + ", price=" + price + ", size=" + size + "]";
  }

    private String orderId;
    private int price;
    private int size;
    private Long time = System.currentTimeMillis();

    public Ask(String orderId, int price, int size) {
        this.orderId = orderId;
        this.price = price;
        this.size = size;
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
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Ask ask = (Ask) o;

        return orderId.equals(ask.orderId);

    }

    @Override
    public int hashCode() {
        return orderId.hashCode();
    }

    public Long time() {
      return time ;
    }
}