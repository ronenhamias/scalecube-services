package io.scalecube.services.benchmarks.jsonmapping;


import io.scalecube.services.api.NullData;
import io.scalecube.services.api.Qualifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class FlatServiceMessage {

  /**
   * This header is supposed to be used by application in case if same data type can be reused for several messages so
   * it will allow to qualify the specific message type.
   */
  private String q;
  /**
   * This is a system header which used by transport for serialization and deserialization purpose. It is not supposed
   * to be used by application directly and it is subject to changes in future releases.
   */
  private String dataType;
  private Map<String, String> headers = Collections.emptyMap();
  private Object data = NullData.NULL_DATA;

  /**
   * Instantiates new empty message builder.
   *
   * @return new builder
   */
  public static Builder builder() {
    return Builder.getInstance();
  }

  // todo
  // /**
  // * Instantiates new message with the same data and headers as at given message.
  // *
  // * @param message the message to be copied
  // * @return a new message, with the same data and headers
  // */
  // public static Builder from(FlatServiceMessage message) {
  // return FlatServiceMessage.builder()
  // .data(message.data())
  // .headers(message.headers());
  // }

  /**
   * Instantiates empty message for deserialization purpose.
   */
  FlatServiceMessage() {}

  private FlatServiceMessage(Builder builder) {
    this.q = builder.q;
    this.dataType = builder.dataType;
    this.headers = builder.headers;
    this.data = builder.data;
  }

  /**
   * Returns the message headers.
   *
   * @return message headers
   */
  public Map<String, String> headers() {
    return headers;
  }

  /**
   * Returns header value by given header name.
   * 
   * @param name header name
   * @return the message header by given header name
   */
  public String header(String name) {
    return headers.get(name);
  }

  /**
   * Returns message qualifier.
   * 
   * @return qualifier string
   */
  public String qualifier() {
    return q;
  }

  /**
   * Returns data format of the message data.
   *
   * @return data format of the data
   */
  public String dataFormat() {
    return "application/json";
  }

  public String dataType() {
    return dataType;
  }

  /**
   * Return the message data, which can be byte array, string or any type.
   *
   * @param <T> data type
   * @return payload of the message or null if message is without any payload
   */
  public <T> T data() {
    // noinspection unchecked
    return (T) data;
  }

  public boolean hasData() {
    return data != NullData.NULL_DATA;
  }

  public boolean hasData(Class<?> dataClass) {
    Objects.requireNonNull(dataClass);
    if (dataClass.isPrimitive()) {
      return hasData();
    } else {
      return dataClass.isInstance(data);
    }
  }

  @Override
  public String toString() {
    return "ServiceMessage {headers: " + headers + ", data: " + data + '}';
  }

  public static class Builder {

    private String q;
    private String dataType;
    private Map<String, String> headers = new HashMap<>();
    private Object data = NullData.NULL_DATA;

    private Builder() {}

    static Builder getInstance() {
      return new Builder();
    }

    public Builder qualifier(String qualifier) {
      this.q = qualifier;
      return this;
    }

    public Builder qualifier(String serviceName, String methodName) {
      return qualifier(Qualifier.asString(serviceName, methodName));
    }

    public Builder dataType(Class<?> dataType) {
      this.dataType = dataType.getName();
      return this;
    }

    public Builder dataType(String dataType) {
      this.dataType = dataType;
      return this;
    }

    public Builder headers(Map<String, String> headers) {
      this.headers.putAll(headers);
      return this;
    }

    public Builder header(String key, String value) {
      this.headers.put(key, value);
      return this;
    }

    public Builder data(Object data) {
      this.data = Objects.requireNonNull(data);
      return this;
    }

    public FlatServiceMessage build() {
      return new FlatServiceMessage(this);
    }
  }
}
