package io.scalecube.services.benchmarks;

public class ServicesBenchmarksSettings extends GenericBenchmarksSettings {

  private static final int RESPONSE_COUNT = 1000;

  private final int responseCount;

  public static Builder from(String[] args) {
    return new Builder().from(args);
  }

  private ServicesBenchmarksSettings(Builder builder) {
    super(builder);
    this.responseCount = builder.responseCount;
  }

  public int responseCount() {
    return responseCount;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String toString() {
    return "ServicesBenchmarksSettings{" +
        "responseCount=" + responseCount +
        "} " + super.toString();
  }

  public static class Builder extends GenericBenchmarksSettings.GenericBuilder<ServicesBenchmarksSettings, Builder> {
    private Integer responseCount = RESPONSE_COUNT;

    private Builder() {
      addArgsConsumer("responseCount", value -> responseCount(Integer.parseInt(value)));
    }

    public Builder responseCount(Integer responseCount) {
      this.responseCount = responseCount;
      return self();
    }

    @Override
    public ServicesBenchmarksSettings build() {
      return new ServicesBenchmarksSettings(this);
    }
  }
}
