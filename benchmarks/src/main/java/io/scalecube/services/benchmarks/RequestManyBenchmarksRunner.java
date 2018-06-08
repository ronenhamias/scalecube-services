package io.scalecube.services.benchmarks;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

import java.util.stream.LongStream;

import reactor.core.publisher.Flux;

public class RequestManyBenchmarksRunner {

  public static void main(String[] args) {
    ServicesBenchmarksSettings settings = ServicesBenchmarksSettings.from(args)
        .responseCount(10000)
        .build();
    ServicesBenchmarksState state = new ServicesBenchmarksState(settings, new BenchmarkServiceImpl());
    state.setup();

    BenchmarkService benchmarkService = state.service(BenchmarkService.class);
    int responseCount = settings.responseCount();
    Timer timer = state.timer();
    Meter meter = state.registry().meter("requestMany/responses");
    Flux.merge(Flux.fromStream(LongStream.range(0, Long.MAX_VALUE).boxed())
        .parallel(Runtime.getRuntime().availableProcessors())
        .runOn(state.scheduler())
        .map(i -> {
          Timer.Context timeContext = timer.time();
          return benchmarkService.requestMany(responseCount)
              .doOnNext(onNext->meter.mark())
              .doFinally(next -> timeContext.stop());
        }))
        .take(settings.executionTaskTime())
        .blockLast();

    state.tearDown();
  }
}
