package io.scalecube.services.benchmarks;

import io.scalecube.services.Microservices;
import io.scalecube.services.ServiceEndpoint;
import io.scalecube.services.benchmarks.jmh.RouterBenchmarks;
import io.scalecube.services.discovery.ServiceScanner;
import io.scalecube.services.registry.ServiceRegistryImpl;
import io.scalecube.services.routing.RoundRobinServiceRouter;
import io.scalecube.services.routing.Router;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class RouterBenchmarksState extends GenericBenchmarksState {

  private static final int IDENTICAL_REFERENCE_COUNT = 10;

  private final ServiceRegistryImpl serviceRegistry = new ServiceRegistryImpl();
  private final Router router = new RoundRobinServiceRouter();

  public RouterBenchmarksState(BenchmarksSettings settings) {
    super(settings);
    int identicalReferenceCount = Integer.parseInt(BenchmarksSettings.find(args, "responseCount", RESPONSE_COUNT));

    List<Microservices.ServiceInfo> services =
        Collections.singletonList(new Microservices.ServiceInfo(new RouterBenchmarks.RouterBenchmarksServiceImpl()));
    IntStream.rangeClosed(0, settings.identicalReferenceCount()).forEach(i -> {
      Map<String, String> tags = new HashMap<>();
      tags.put("k1-" + i, "v1-" + i);
      tags.put("k2-" + i, "v2-" + i);
      ServiceEndpoint serviceEndpoint = ServiceScanner.scan(services, "localhost" + i, i, tags);
      serviceRegistry.registerService(serviceEndpoint);
    });
  }

  public Router getRouter() {
    return router;
  }

  public ServiceRegistryImpl getServiceRegistry() {
    return serviceRegistry;
  }
}
