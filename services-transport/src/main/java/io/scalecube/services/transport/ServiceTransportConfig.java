package io.scalecube.services.transport;

import java.util.concurrent.ExecutorService;

public class ServiceTransportConfig {

  private final ExecutorService clientEventLoopGroup;
  private final ExecutorService serverEventLoopGroup;

  private ServiceTransportConfig(Builder builder) {
    this.clientEventLoopGroup = builder.clientEventLoopGroup;
    this.serverEventLoopGroup = builder.serverEventLoopGroup;
  }

  public ExecutorService clientEventLoopGroup() {
    return clientEventLoopGroup;
  }

  public ExecutorService serverEventLoopGroup() {
    return serverEventLoopGroup;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    ExecutorService clientEventLoopGroup;
    ExecutorService serverEventLoopGroup;

    private Builder() {}

    public Builder clientEventLoopGroup(ExecutorService clientEventLoopGroup) {
      this.clientEventLoopGroup = clientEventLoopGroup;
      return this;
    }

    public Builder serverEventLoopGroup(ExecutorService serverEventLoopGroup) {
      this.serverEventLoopGroup = serverEventLoopGroup;
      return this;
    }

    public ServiceTransportConfig build() {
      return new ServiceTransportConfig(this);
    }

  }

}
