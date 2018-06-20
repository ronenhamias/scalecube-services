package io.scalecube.services.benchmarks;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

import java.util.stream.LongStream;

import reactor.core.publisher.Flux;

public class OneWayBenchmarksRunner {

  public static void main(String[] args) {
    BenchmarksSettings settings = BenchmarksSettings.from(args).build();
    ServicesBenchmarksState state = new ServicesBenchmarksState(settings, new BenchmarkServiceImpl());
    state.setup();

    BenchmarkService benchmarkService = state.service(BenchmarkService.class);
    Timer timer = state.timer();
    Meter throutput = state.throutput();

    Flux.merge(Flux.fromStream(LongStream.range(0, Long.MAX_VALUE).boxed())
        .publishOn(state.scheduler())
        .map(i -> {
          Timer.Context timeContext = timer.time();
          return benchmarkService.oneWay("hello").doOnTerminate(() -> {
            timeContext.stop();
            throutput.mark();
          });
        }))
        .take(settings.executionTaskTime())
        .blockLast();

    state.tearDown();
  }
}
