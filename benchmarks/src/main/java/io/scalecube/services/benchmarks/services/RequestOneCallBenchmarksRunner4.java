package io.scalecube.services.benchmarks.services;

import static io.scalecube.services.benchmarks.services.BenchmarkService.REQUEST_ONE;

import io.scalecube.benchmarks.BenchmarksSettings;
import io.scalecube.services.ServiceCall;

import com.codahale.metrics.Timer;

import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import reactor.core.publisher.Flux;

public class RequestOneCallBenchmarksRunner4 {

  public static void main(String[] args) throws InterruptedException {
    BenchmarksSettings settings = BenchmarksSettings.from(args).build();
    ServicesBenchmarksState state = new ServicesBenchmarksState(settings, new BenchmarkServiceImpl());


    try {
      state.start();

      Timer timer = state.timer("timer");

      ServiceCall serviceCall = state.serviceCall();
      Flux.merge(
          Flux.fromStream(LongStream.range(0, settings.numOfIterations()).boxed())
              .parallel(settings.nThreads())
              .runOn(state.scheduler())
              .map(ii -> {
                Timer.Context timeContext = timer.time();
                return serviceCall.requestOne(REQUEST_ONE)
                    .doOnNext(msg -> ReferenceCountUtil.safeRelease(msg.data()))
                    .doOnTerminate(timeContext::stop);
              }))
          .subscribe();

      TimeUnit.SECONDS.sleep(settings.executionTaskDuration().getSeconds());

    } finally {
      state.shutdown();
    }
  }
}
