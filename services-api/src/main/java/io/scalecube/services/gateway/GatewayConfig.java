package io.scalecube.services.gateway;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents gateway configuration.
 */
public final class GatewayConfig {

  private final Map<String, Object> options;

  private Integer port;

  private GatewayConfig(Builder builder) {
    port = builder.port;
    options = Collections.unmodifiableMap(builder.options);
  }

  /**
   * Gateway port.
   * 
   * @return optional value of port
   */
  public Optional<Integer> port() {
    return Optional.of(port);
  }

  /**
   * Specific configuration for each particular gateway defined as key-value pairs.
   *
   * @return map of options
   */
  public Map<String, Object> options() {
    return options;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String toString() {
    return "GatewayConfig{" +
        "options=" + options +
        ", port=" + port +
        '}';
  }

  public static class Builder {

    private final Map<String, Object> options = new HashMap<>();

    private Integer port;

    private Builder() {}

    public Builder port(int port) {
      this.port = port;
      return this;
    }

    public Builder addOption(String key, Object value) {
      options.put(key, value);
      return this;
    }

    public Builder addOptions(Map<String, Object> options) {
      options.putAll(options);
      return this;
    }

    public GatewayConfig build() {
      return new GatewayConfig(this);
    }

  }
}
