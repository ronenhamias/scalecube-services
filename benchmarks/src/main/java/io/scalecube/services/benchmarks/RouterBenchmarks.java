package io.scalecube.services.benchmarks;

import io.scalecube.services.ServiceReference;
import io.scalecube.services.api.Qualifier;
import io.scalecube.services.api.ServiceMessage;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

import java.util.stream.LongStream;

import reactor.core.publisher.Flux;

public class RouterBenchmarks {

  private static final String NAMESPACE = "benchmark";
  private static final String ACTION = "method1";
  private static final ServiceMessage MESSAGE = ServiceMessage.builder()
      .qualifier(Qualifier.asString(NAMESPACE, ACTION))
      .build();

  public static void main(String[] args) {
    BenchmarksSettings settings = BenchmarksSettings.from(args).build();
    RouterBenchmarksState state = new RouterBenchmarksState(settings);
    state.setup();

    Timer timer = state.timer();
    Meter throutput = state.throutput();

    Flux.merge((Flux<ServiceReference>) Flux.fromStream(LongStream.range(0, Long.MAX_VALUE).boxed())
        .publishOn(state.scheduler())
        .map(i -> {
          Timer.Context timeContext = timer.time();
          ServiceReference serviceReference = state.getRouter().route(state.getServiceRegistry(), MESSAGE).get();
          timeContext.stop();
          throutput.mark();
          return serviceReference;
        }))
        .take(settings.executionTaskTime())
        .blockLast();

    state.tearDown();
  }
}
