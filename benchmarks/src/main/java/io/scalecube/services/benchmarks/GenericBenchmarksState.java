package io.scalecube.services.benchmarks;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class GenericBenchmarksState {

  protected final GenericBenchmarksSettings settings;

  protected MetricRegistry registry;
  protected ConsoleReporter consoleReporter;
  protected Scheduler scheduler;
  protected CsvReporter csvReporter;


  public GenericBenchmarksState(GenericBenchmarksSettings settings) {
    this.settings = settings;
  }

  protected void beforeAll() {}

  protected void afterAll() {}

  public final void setup() {
    registry = new MetricRegistry();

    consoleReporter = ConsoleReporter.forRegistry(registry)
        .outputTo(System.err)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .convertRatesTo(TimeUnit.SECONDS)
        .build();

    csvReporter = CsvReporter.forRegistry(registry)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
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

  public final void tearDown() {
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

  public MetricRegistry registry() {
    return registry;
  }

  public Scheduler scheduler() {
    return scheduler;
  }

  public Timer timer() {
    return registry.timer(settings.taskName() + "-timer");
  }

  public Meter meter(String name) {
    return registry.meter(settings.taskName() + "-" + name);
  }

  public Meter throutput() {
    return meter("throughput");
  }

  public Histogram histogram(String name) {
    return registry.histogram(settings.taskName() + "-" + name);
  }

}
