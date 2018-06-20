package io.scalecube.services.benchmarks;

import static io.scalecube.services.benchmarks.BenchmarkService.REQUEST_ONE;

import io.scalecube.services.ServiceCall;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

import java.util.stream.LongStream;

import reactor.core.publisher.Flux;

public class RequestOneCallBenchmarksRunner {

  public static void main(String[] args) {
    BenchmarksSettings settings = BenchmarksSettings.from(args).build();
    ServicesBenchmarksState state = new ServicesBenchmarksState(settings, new BenchmarkServiceImpl());
    state.setup();

    ServiceCall serviceCall = state.seed().call().create();
    Timer timer = state.timer();
    Meter throutput = state.throutput();

    Flux.merge(Flux.fromStream(LongStream.range(0, Long.MAX_VALUE).boxed())
        .publishOn(state.scheduler())
        .map(i -> {
          Timer.Context timeContext = timer.time();
          return serviceCall.requestOne(REQUEST_ONE)
              .doOnNext(next -> {
                timeContext.stop();
                throutput.mark();
              });
        }))
        .take(settings.executionTaskTime())
        .blockLast();

    state.tearDown();
  }
}
