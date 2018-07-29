package io.scalecube.services.benchmarks.services;

import static io.scalecube.services.benchmarks.services.BenchmarkService.REQUEST_ONE;

import io.scalecube.benchmarks.BenchmarksSettings;
import io.scalecube.services.ServiceCall;

import com.codahale.metrics.Timer;

import io.netty.util.ReferenceCountUtil;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class RequestOneCallBenchmarksRunner2 {

  public static void main(String[] args) throws InterruptedException {
    BenchmarksSettings settings = BenchmarksSettings.from(args)
        .executionTaskDuration(Duration.ofMinutes(2))
        .build();
    ServicesBenchmarksState state = new ServicesBenchmarksState(settings, new BenchmarkServiceImpl());


    try {
      state.start();

      Timer timer = state.timer("timer");

      for (int i = 0; i < settings.nThreads(); i++) {
        ServiceCall serviceCall = state.serviceCall();
        Scheduler scheduler = Schedulers.newSingle("i" + i, true);
        Flux.merge(
            Flux.fromStream(LongStream.range(0, settings.numOfIterations()).boxed())
                .publishOn(scheduler)
                // .subscribeOn(scheduler)
                // .publishOn(state.scheduler())
                .map(ii -> {
                  Timer.Context timeContext = timer.time();
                  return serviceCall.requestOne(REQUEST_ONE)
                      .doOnNext(msg -> ReferenceCountUtil.safeRelease(msg.data()))
                      .doOnTerminate(timeContext::stop);
                }))
            .subscribe();
      }

      TimeUnit.SECONDS.sleep(settings.executionTaskDuration().getSeconds());

    } finally {
      state.shutdown();
    }
  }
}
