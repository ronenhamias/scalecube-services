package io.scalecube.services.transport;

import java.util.concurrent.ExecutorService;

public class ServiceTransportConfig {

  private final ExecutorService clientEventLoopGroup;
  private final ExecutorService serverEventLoopGroup;

  private ServiceTransportConfig(Builder builder) {
    this.clientEventLoopGroup = builder.clientExecutorService;
    this.serverEventLoopGroup = builder.serverExecutorService;
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
    ExecutorService clientExecutorService;
    ExecutorService serverExecutorService;

    private Builder() {}

    public Builder clientExecutorService(ExecutorService clientExecutorService) {
      this.clientExecutorService = clientExecutorService;
      return this;
    }

    public Builder serverExecutorService(ExecutorService serverExecutorService) {
      this.serverExecutorService = serverExecutorService;
      return this;
    }

    public ServiceTransportConfig build() {
      return new ServiceTransportConfig(this);
    }

  }

}
