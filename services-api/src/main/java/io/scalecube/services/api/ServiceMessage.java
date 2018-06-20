package io.scalecube.services.api;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ServiceMessage {

  /**
   * This header is supposed to be used by application in case if same data type can be reused for several messages so
   * it will allow to qualify the specific message type.
   */
  static final String HEADER_QUALIFIER = "q";


  /**
   * This header stands for "Stream Id" and has to be used for Stream multiplexing. Messages within one logical stream
   * have to be signed with equal sid-s.
   */
  static final String HEADER_STREAM_ID = "sid";

  /**
   * This is a system header which used by transport for serialization and deserialization purpose. It is not supposed
   * to be used by application directly and it is subject to changes in future releases.
   */
  static final String HEADER_DATA_TYPE = "_type";
  static final String HEADER_DATA_FORMAT = "_data_format";
  static final String HEADER_INACTIVITY = "_inactivity";

  private String q;
  private String sid;
  private String type;
  private String dataFormat;
  private String inactivity;
  private Map<String, String> headers = Collections.emptyMap();
  private Object data = NullData.NULL_DATA;

  /**
   * Instantiates empty message for deserialization purpose.
   */
  ServiceMessage() {}

  private ServiceMessage(Builder builder) {
    this.q = builder.q;
    this.sid = builder.sid;
    this.type = builder.type;
    this.dataFormat = builder.dataFormat;
    this.inactivity = builder.inactivity;
    this.headers = builder.headers;
    this.data = builder.data;
  }

  /**
   * Instantiates new message with the same data and headers as at given message.
   * 
   * @param message the message to be copied
   * @return a new message, with the same data and headers
   */
  public static Builder from(ServiceMessage message) {
    return ServiceMessage.builder()
        .qualifier(message.q)
        .streamId(message.sid)
        .dataType(message.type)
        .dataFormat(message.dataFormat)
        .inactivity(message.inactivity)
        .headers(message.headers)
        .data(message.data);
  }

  /**
   * Instantiates new empty message builder.
   *
   * @return new builder
   */
  public static Builder builder() {
    return Builder.getInstance();
  }

  /**
   * Sets data for deserialization purpose.
   * 
   * @param data data to set
   */
  void setData(Object data) {
    this.data = Objects.requireNonNull(data);
  }

  /**
   * Sets headers for deserialization purpose.
   * 
   * @param headers headers to set
   */
  void setHeaders(Map<String, String> headers) {
    this.headers = Collections.unmodifiableMap(headers);
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
   * Returns message's qualifier.
   * 
   * @return qualifier string
   */
  public String qualifier() {
    return q;
  }

  /**
   * Returns message's sid.
   *
   * @return streamId.
   */
  public String streamId() {
    return sid;
  }

  /**
   * Returns secound inactivity optional timeout to cancel this request if not provided then infinite
   * 
   * @return secound inactivity
   */
  public String inactivity() {
    return inactivity;
  }

  /**
   * Returns data format of the message data.
   *
   * @return data format of the data
   */
  public String dataFormat() {
    return dataFormat;
  }

  /**
   * Returns data type of the message data. This is a system property which used by transport for serialization and
   * deserialization purpose. It is not supposed to be used by application directly and it is subject to changes in
   * future releases.
   *
   * @return data type of the data
   */
  public String dataType() {
    return type;
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
    return new StringBuilder("ServiceMessage{")
        .append("qualifier='").append(q).append('\'')
        .append(", streamId='").append(sid).append('\'')
        .append(", dataType='").append(type).append('\'')
        .append(", dataFormat='").append(dataFormat).append('\'')
        .append(", inactivity='").append(inactivity).append('\'')
        .append(", headers=").append(headers)
        .append(", data=").append(data)
        .append('}')
        .toString();
  }

  public static class Builder {

    private String q;
    private String sid;
    private String type;
    private String dataFormat;
    private String inactivity;
    private Map<String, String> headers = new HashMap<>();
    private Object data = NullData.NULL_DATA;

    private Builder() {}

    static Builder getInstance() {
      return new Builder();
    }

    public Builder data(Object data) {
      this.data = Objects.requireNonNull(data);
      return this;
    }

    public Builder dataType(String dataType) {
      this.type = dataType;
      return this;
    }

    public Builder dataType(Class<?> dataType) {
      return dataType(dataType.getName());
    }

    public Builder dataFormat(String dataFormat) {
      this.dataFormat = dataFormat;
      return this;
    }

    public Builder headers(Map<String, String> headers) {
      this.headers.putAll(headers);
      return this;
    }

    public Builder header(String key, String value) {
      headers.put(key, value);
      return this;
    }

    public Builder qualifier(String serviceName, String methodName) {
      return qualifier(Qualifier.asString(serviceName, methodName));
    }

    public Builder qualifier(String qualifier) {
      this.q = qualifier;
      return this;
    }

    public Builder streamId(String streamId) {
      this.sid = streamId;
      return this;
    }

    public Builder inactivity(String inactivity) {
      this.inactivity = inactivity;
      return this;
    }

    public ServiceMessage build() {
      return new ServiceMessage(this);
    }
  }
}
