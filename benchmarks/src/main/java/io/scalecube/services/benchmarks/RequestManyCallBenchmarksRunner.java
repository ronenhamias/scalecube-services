package io.scalecube.services.benchmarks;

import io.scalecube.services.ServiceCall;
import io.scalecube.services.api.ServiceMessage;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

import java.util.stream.LongStream;

import reactor.core.publisher.Flux;

public class RequestManyCallBenchmarksRunner {

  public static void main(String[] args) {
    BenchmarksSettings settings = BenchmarksSettings.from(args).build();
    ServicesBenchmarksState state = new ServicesBenchmarksState(settings, new BenchmarkServiceImpl());
    state.setup();

    ServiceCall serviceCall = state.seed().call().create();
    int responseCount = settings.responseCount();
    Timer timer = state.timer();
    Meter throutput = state.throutput();

    ServiceMessage message = ServiceMessage.builder()
        .qualifier(BenchmarkService.class.getName(), "requestMany")
        .data(responseCount)
        .build();

    Flux.merge(Flux.fromStream(LongStream.range(0, Long.MAX_VALUE).boxed())
        .publishOn(state.scheduler())
        .map(i -> {
          Timer.Context timeContext = timer.time();
          return serviceCall.requestMany(message).doOnNext(next -> {
            timeContext.stop();
            throutput.mark();
          });
        }))
        .take(settings.executionTaskTime())
        .blockLast();

    state.tearDown();
  }
}
