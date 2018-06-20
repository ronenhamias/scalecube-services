package io.scalecube.services.benchmarks;

import io.scalecube.services.Microservices;

public class ServicesBenchmarksState extends GenericBenchmarksState {

  private final Object[] services;

  private Microservices seed;
  private Microservices node;

  public ServicesBenchmarksState(BenchmarksSettings settings, Object... services) {
    super(settings);
    this.services = services;
  }

  @Override
  public void beforeAll() {
    seed = Microservices.builder()
        .metrics(registry())
        .startAwait();

    node = Microservices.builder()
        .metrics(registry())
        .seeds(seed.cluster().address())
        .services(services)
        .startAwait();

    System.err.println("seed address: " + seed.cluster().address() +
        ", services address: " + node.serviceAddress() +
        ", seed serviceRegistry: " + seed.serviceRegistry().listServiceReferences());
  }

  @Override
  public void afterAll() {
    if (node != null) {
      node.shutdown().block();
    }

    if (seed != null) {
      seed.shutdown().block();
    }
  }

  public Microservices seed() {
    return seed;
  }

  public <T> T service(Class<T> c) {
    return seed.call().create().api(c);
  }

}
