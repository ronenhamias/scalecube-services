package io.scalecube.services.benchmarks;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

import java.util.stream.LongStream;

import reactor.core.publisher.Flux;

public class RequestManyBenchmarksRunner {

  private static final String RESPONSE_COUNT = "1000";

  public static void main(String[] args) {
    BenchmarksSettings settings = BenchmarksSettings.from(args)
        .build();

    ServicesBenchmarksState state = new ServicesBenchmarksState(settings, new BenchmarkServiceImpl());
    state.setup();

    BenchmarkService benchmarkService = state.service(BenchmarkService.class);
    int responseCount = Integer.parseInt(BenchmarksSettings.find(args, "responseCount", RESPONSE_COUNT));
    Timer timer = state.timer();
    Meter meter = state.meter("responses");
    Meter throutput = state.throutput();

    Flux.merge(Flux.fromStream(LongStream.range(0, Long.MAX_VALUE).boxed())
        .parallel()
        .runOn(state.scheduler())
        .map(i -> {
          Timer.Context timeContext = timer.time();
          return benchmarkService.requestMany(responseCount)
              .doOnNext(onNext -> meter.mark())
              .doFinally(next -> {
                timeContext.stop();
                throutput.mark();
              });
        }))
        .take(settings.executionTaskTime())
        .blockLast();

    state.tearDown();
  }
}
