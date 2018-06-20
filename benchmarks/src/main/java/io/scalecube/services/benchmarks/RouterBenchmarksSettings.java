package io.scalecube.services.benchmarks;

public class RouterBenchmarksSettings extends GenericBenchmarksSettings {

  private static final int IDENTICAL_REFERENCE_COUNT = 10;

  private final int identicalReferenceCount;

  public static Builder from(String[] args) {
    return new Builder().from(args);
  }

  private RouterBenchmarksSettings(Builder builder) {
    super(builder);
    this.identicalReferenceCount = builder.identicalRefCount;
  }

  public int identicalReferenceCount() {
    return identicalReferenceCount;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String toString() {
    return "RouterBenchmarksSettings{" +
        "identicalReferenceCount=" + identicalReferenceCount +
        "} " + super.toString();
  }

  public static class Builder extends GenericBuilder<RouterBenchmarksSettings, Builder> {
    private Integer identicalRefCount = IDENTICAL_REFERENCE_COUNT;

    private Builder() {
      addArgsConsumer("identicalReferenceCount", value -> identicalRefCount(Integer.parseInt(value)));
    }

    public Builder identicalRefCount(Integer identicalRefCount) {
      this.identicalRefCount = identicalRefCount;
      return self();
    }

    @Override
    public RouterBenchmarksSettings build() {
      return new RouterBenchmarksSettings(this);
    }
  }
}
