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

public class BenchmarksSettings {

  private static final int N_THREADS = Runtime.getRuntime().availableProcessors();
  private static final Duration EXECUTION_TASK_TIME = Duration.ofSeconds(60);
  private static final Duration REPORTER_PERIOD = Duration.ofSeconds(10);
  private static final int RESPONSE_COUNT = 1000;
  private static final int IDENTICAL_REFERENCE_COUNT = 10;

  private final int nThreads;
  private final Duration executionTaskTime;
  private final Duration reporterPeriod;
  private final File csvReporterDirectory;
  private final String taskName;

  private final int responseCount;
  private final int identicalReferenceCount;

  public static GenericBuilder from(String[] args) {
    return new GenericBuilder().from(args);
  }

  private BenchmarksSettings(GenericBuilder builder) {
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

    this.responseCount = builder.responseCount;
    this.identicalReferenceCount = builder.identicalReferenceCount;
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


  public int responseCount() {
    return responseCount;
  }

  public int identicalReferenceCount() {
    return identicalReferenceCount;
  }

  @Override
  public String toString() {
    return "BenchmarksSettings{" +
        "nThreads=" + nThreads +
        ", executionTaskTime=" + executionTaskTime +
        ", reporterPeriod=" + reporterPeriod +
        ", csvReporterDirectory=" + csvReporterDirectory +
        ", taskName='" + taskName + '\'' +
        '}';
  }

  public static class GenericBuilder {
    private final Map<String, Consumer<String>> argsConsumers;

    private Integer nThreads = N_THREADS;
    private Duration executionTaskTime = EXECUTION_TASK_TIME;
    private Duration reporterPeriod = REPORTER_PERIOD;

    private Integer responseCount = RESPONSE_COUNT;
    private Integer identicalReferenceCount = IDENTICAL_REFERENCE_COUNT;

    public GenericBuilder from(String[] args) {
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
      return this;
    }

    private GenericBuilder() {
      this.argsConsumers = new HashMap<>();
      this.argsConsumers.put("nThreads", value -> nThreads(Integer.parseInt(value)));
      this.argsConsumers.put("executionTaskTimeInSec",
          value -> executionTaskTime(Duration.ofSeconds(Long.parseLong(value))));
      this.argsConsumers.put("reporterPeriodInSec", value -> reporterPeriod(Duration.ofSeconds(Long.parseLong(value))));

      this.argsConsumers.put("responseCount", value -> responseCount(Integer.parseInt(value)));
      this.argsConsumers.put("identicalReferenceCount", value -> identicalRefCount(Integer.parseInt(value)));
    }

    public GenericBuilder nThreads(Integer nThreads) {
      this.nThreads = nThreads;
      return this;
    }

    public GenericBuilder executionTaskTime(Duration executionTaskTime) {
      this.executionTaskTime = executionTaskTime;
      return this;
    }

    public GenericBuilder reporterPeriod(Duration reporterPeriod) {
      this.reporterPeriod = reporterPeriod;
      return this;
    }

    public GenericBuilder responseCount(Integer responseCount) {
      this.responseCount = responseCount;
      return this;
    }

    public GenericBuilder identicalRefCount(Integer identicalReferenceCount) {
      this.identicalReferenceCount = identicalReferenceCount;
      return this;
    }

    public BenchmarksSettings build() {
      return new BenchmarksSettings(this);
    }

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
