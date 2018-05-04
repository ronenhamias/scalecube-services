package io.scalecube.examples.orderbook.service.engine;

public interface Order {

  public int getPrice();

  public int getUnits();

  public Long time();

}
