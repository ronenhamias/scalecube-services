package io.scalecube.services.benchmarks;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GenericBenchmarksSettings {

  private static final int N_THREADS = Runtime.getRuntime().availableProcessors();
  private static final Duration EXECUTION_TASK_TIME = Duration.ofSeconds(60);
  private static final Duration REPORTER_PERIOD = Duration.ofSeconds(10);

  private final int nThreads;
  private final Duration executionTaskTime;
  private final Duration reporterPeriod;
  private final File csvReporterDirectory;
  private final String taskName;

  protected GenericBenchmarksSettings(GenericBuilder builder) {
    this.nThreads = builder.nThreads;
    this.executionTaskTime = builder.executionTaskTime;
    this.reporterPeriod = builder.reporterPeriod;

    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    this.taskName = stackTrace[stackTrace.length - 1].getClassName();

    String time = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    this.csvReporterDirectory = Paths.get("benchmarks", "results", taskName, time).toFile();
    // noinspection ResultOfMethodCallIgnored
    this.csvReporterDirectory.mkdirs();
  }

  public int nThreads() {
    return nThreads;
  }

  public Duration executionTaskTime() {
    return executionTaskTime;
  }

  public Duration reporterPeriod() {
    return reporterPeriod;
  }

  public File csvReporterDirectory() {
    return csvReporterDirectory;
  }

  public String taskName() {
    return taskName;
  }

  @Override
  public String toString() {
    return "GenericBenchmarksSettings{" +
        "nThreads=" + nThreads +
        ", executionTaskTime=" + executionTaskTime +
        ", reporterPeriod=" + reporterPeriod +
        ", csvReporterDirectory=" + csvReporterDirectory +
        ", taskName='" + taskName + '\'' +
        '}';
  }

  public static abstract class GenericBuilder<TYPE, SELF extends GenericBuilder<TYPE, SELF>> {
    private final Map<String, Consumer<String>> argsConsumers;

    private Integer nThreads = N_THREADS;
    private Duration executionTaskTime = EXECUTION_TASK_TIME;
    private Duration reporterPeriod = REPORTER_PERIOD;

    public SELF from(String[] args) {
      if (args != null) {
        for (String pair : args) {
          String[] keyValue = pair.split("=", 2);
          String key = keyValue[0];
          String value = keyValue[1];
          Consumer<String> consumer = argsConsumers.get(key);
          if (consumer != null) {
            consumer.accept(value);
          } else {
            throw new IllegalArgumentException("unknown command: " + pair);
          }
        }
      }
      return self();
    }

    protected GenericBuilder() {
      this.argsConsumers = new HashMap<>();
      this.argsConsumers.put("nThreads", value -> nThreads(Integer.parseInt(value)));
      this.argsConsumers.put("executionTaskTimeInSec",
          value -> executionTaskTime(Duration.ofSeconds(Long.parseLong(value))));
      this.argsConsumers.put("reporterPeriodInSec", value -> reporterPeriod(Duration.ofSeconds(Long.parseLong(value))));
    }

    protected void addArgsConsumer(String key, Consumer<String> consumer) {
      this.argsConsumers.put(key, consumer);
    }

    public SELF nThreads(Integer nThreads) {
      this.nThreads = nThreads;
      return self();
    }

    public SELF executionTaskTime(Duration executionTaskTime) {
      this.executionTaskTime = executionTaskTime;
      return self();
    }

    public SELF reporterPeriod(Duration reporterPeriod) {
      this.reporterPeriod = reporterPeriod;
      return self();
    }

    public SELF self() {
      // noinspection unchecked
      return (SELF) this;
    }

    public abstract TYPE build();

    private void parse(String[] args, Map<String, Consumer<String>> consumers) {
      if (args != null) {
        for (String pair : args) {
          String[] keyValue = pair.split("=", 2);
          String key = keyValue[0];
          String value = keyValue[1];
          Consumer<String> consumer = consumers.get(key);
          if (consumer != null) {
            consumer.accept(value);
          } else {
            throw new IllegalArgumentException("unknown command: " + pair);
          }
        }
      }
    }
  }
}
