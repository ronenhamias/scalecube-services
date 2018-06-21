package io.scalecube.services.benchmarks;

import com.codahale.metrics.Timer;

public class OneWayBenchmarksRunner {

  public static void main(String[] args) {
    BenchmarksSettings settings = BenchmarksSettings.from(args).build();
    ServicesBenchmarksState state = new ServicesBenchmarksState(settings, new BenchmarkServiceImpl());

    state.blockLastPublisher(benchmarksState -> {

      BenchmarkService benchmarkService = state.service(BenchmarkService.class);
      Timer timer = state.timer("timer");

      return i -> {
        Timer.Context timeContext = timer.time();
        return benchmarkService.oneWay("hello").doOnTerminate(timeContext::stop);
      };
    });
  }
}
