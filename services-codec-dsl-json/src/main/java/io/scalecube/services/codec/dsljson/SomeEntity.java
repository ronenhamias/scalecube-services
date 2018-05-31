package io.scalecube.services.codec.dsljson;

import java.time.Duration;
import java.time.LocalDateTime;

public class SomeEntity {

  private String id;
  private Integer count;
  private boolean check;
  private Boolean check2;
  private long price;

  private Duration duration;
  private LocalDateTime localDateTime;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public boolean isCheck() {
    return check;
  }

  public void setCheck(boolean check) {
    this.check = check;
  }

  public Boolean getCheck2() {
    return check2;
  }

  public void setCheck2(Boolean check2) {
    this.check2 = check2;
  }

  public long getPrice() {
    return price;
  }

  public void setPrice(long price) {
    this.price = price;
  }

  public Duration getDuration() {
    return duration;
  }

  public void setDuration(Duration duration) {
    this.duration = duration;
  }

  public LocalDateTime getLocalDateTime() {
    return localDateTime;
  }

  public void setLocalDateTime(LocalDateTime localDateTime) {
    this.localDateTime = localDateTime;
  }

  @Override
  public String toString() {
    return "SomeEntity{" +
        "id='" + id + '\'' +
        ", count=" + count +
        ", check=" + check +
        ", check2=" + check2 +
        ", price=" + price +
        ", duration=" + duration +
        ", localDateTime=" + localDateTime +
        '}';
  }
}
