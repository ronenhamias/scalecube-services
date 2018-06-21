package io.scalecube.services.benchmarks;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.LongStream;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class BenchmarksState {

  private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarksState.class);

  protected final BenchmarksSettings settings;

  private ConsoleReporter consoleReporter;
  private Scheduler scheduler;
  private CsvReporter csvReporter;

  public BenchmarksState(BenchmarksSettings settings) {
    this.settings = settings;
  }

  protected void beforeAll() {
    // NOP
  }

  protected void afterAll() {
    // NOP
  }

  private void setUp() {
    LOGGER.info("Benchmarks settings: " + settings);

    consoleReporter = ConsoleReporter.forRegistry(settings.registry())
        .outputTo(System.out)
        .convertDurationsTo(TimeUnit.NANOSECONDS)
        .convertRatesTo(TimeUnit.SECONDS)
        .build();

    csvReporter = CsvReporter.forRegistry(settings.registry())
        .convertDurationsTo(TimeUnit.NANOSECONDS)
        .convertRatesTo(TimeUnit.SECONDS)
        .build(settings.csvReporterDirectory());

    scheduler = Schedulers.fromExecutor(Executors.newFixedThreadPool(settings.nThreads()));

    Duration reporterPeriod = settings.reporterPeriod();
    consoleReporter.start(reporterPeriod.toMillis(), TimeUnit.MILLISECONDS);
    csvReporter.start(reporterPeriod.toMillis(), TimeUnit.MILLISECONDS);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      consoleReporter.report();
      csvReporter.report();
    }));

    beforeAll();
  }

  private void tearDown() {
    if (consoleReporter != null) {
      consoleReporter.report();
      consoleReporter.stop();
    }

    if (csvReporter != null) {
      csvReporter.report();
      csvReporter.stop();
    }

    if (scheduler != null) {
      scheduler.dispose();
    }

    afterAll();
  }

  public Scheduler scheduler() {
    return scheduler;
  }

  public Timer timer(String name) {
    return settings.registry().timer(settings.taskName() + "-" + name);
  }

  public Meter meter(String name) {
    return settings.registry().meter(settings.taskName() + "-" + name);
  }

  public Histogram histogram(String name) {
    return settings.registry().histogram(settings.taskName() + "-" + name);
  }

  public final Object blockLastObject(Function<BenchmarksState, Function<Long, Object>> func) {
    try {
      setUp();
      Function<Long, Object> func1 = func.apply(this);
      return Flux.merge(Flux.fromStream(LongStream.range(0, Long.MAX_VALUE).boxed())
          .publishOn(scheduler())
          .map(func1))
          .take(settings.executionTaskTime())
          .blockLast();
    } finally {
      tearDown();
    }
  }

  public final Object blockLastPublisher(Function<BenchmarksState, Function<Long, Publisher<?>>> func) {
    try {
      setUp();
      Function<Long, Publisher<?>> func1 = func.apply(this);
      return Flux.merge(Flux.fromStream(LongStream.range(0, Long.MAX_VALUE).boxed())
          .publishOn(scheduler())
          .map(func1))
          .take(settings.executionTaskTime())
          .blockLast();
    } finally {
      tearDown();
    }
  }

}
