package io.scalecube.services.benchmarks;

import com.codahale.metrics.MetricRegistry;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class BenchmarksSettings {

  private static final int N_THREADS = Runtime.getRuntime().availableProcessors();
  private static final Duration EXECUTION_TASK_DURATION = Duration.ofSeconds(60);
  private static final Duration EXECUTION_TASK_INTERVAL = Duration.ZERO;
  private static final Duration REPORTER_INTERVAL = Duration.ofSeconds(3);
  private static final TimeUnit DURATION_UNIT = TimeUnit.MILLISECONDS;
  private static final TimeUnit RATE_UNIT = TimeUnit.SECONDS;
  private static final long NUM_OF_ITERATIONS = Long.MAX_VALUE;
  private static final Duration RAMP_UP_DURATION = Duration.ofSeconds(10);
  private static final Duration RAMP_UP_INTERVAL = Duration.ofSeconds(1);

  private final int nThreads;
  private final Duration executionTaskDuration;
  private final Duration executionTaskInterval;
  private final Duration reporterInterval;
  private final File csvReporterDirectory;
  private final String taskName;
  private final TimeUnit durationUnit;
  private final TimeUnit rateUnit;
  private final MetricRegistry registry;
  private final long numOfIterations;
  private final Duration rampUpDuration;
  private final Duration rampUpInterval;

  private final Map<String, String> options;

  public static Builder from(String[] args) {
    return new Builder().from(args);
  }

  private BenchmarksSettings(Builder builder) {
    this.nThreads = builder.nThreads;
    this.executionTaskDuration = builder.executionTaskDuration;
    this.executionTaskInterval = builder.executionTaskInterval;
    this.reporterInterval = builder.reporterInterval;
    this.numOfIterations = builder.numOfIterations;

    this.rampUpDuration = builder.rampUpDuration;
    this.rampUpInterval = builder.rampUpInterval;

    this.options = builder.options;

    this.registry = new MetricRegistry();

    this.durationUnit = builder.durationUnit;
    this.rateUnit = builder.rateUnit;

    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    this.taskName = minifyClassName(stackTrace[stackTrace.length - 1].getClassName());

    String time = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    this.csvReporterDirectory = Paths.get("benchmarks", "results", taskName + allPropertiesAsString(), time).toFile();
    // noinspection ResultOfMethodCallIgnored
    this.csvReporterDirectory.mkdirs();
  }

  public int nThreads() {
    return nThreads;
  }

  public Duration executionTaskDuration() {
    return executionTaskDuration;
  }

  public Duration executionTaskInterval() {
    return executionTaskInterval;
  }

  public Duration reporterInterval() {
    return reporterInterval;
  }

  public File csvReporterDirectory() {
    return csvReporterDirectory;
  }

  public String taskName() {
    return taskName;
  }

  public String find(String key, String defValue) {
    return options.getOrDefault(key, defValue);
  }

  public MetricRegistry registry() {
    return registry;
  }

  public TimeUnit durationUnit() {
    return durationUnit;
  }

  public TimeUnit rateUnit() {
    return rateUnit;
  }

  public long numOfIterations() {
    return numOfIterations;
  }

  public Duration rampUpDuration() {
    return rampUpDuration;
  }

  public Duration rampUpInterval() {
    return rampUpInterval;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("BenchmarksSettings{");
    sb.append("nThreads=").append(nThreads);
    sb.append(", executionTaskDuration=").append(executionTaskDuration);
    sb.append(", executionTaskInterval=").append(executionTaskInterval);
    sb.append(", numOfIterations=").append(numOfIterations);
    sb.append(", reporterInterval=").append(reporterInterval);
    sb.append(", csvReporterDirectory=").append(csvReporterDirectory);
    sb.append(", taskName='").append(taskName).append('\'');
    sb.append(", durationUnit=").append(durationUnit);
    sb.append(", rateUnit=").append(rateUnit);
    sb.append(", rampUpDuration=").append(rampUpDuration);
    sb.append(", rampUpInterval=").append(rampUpInterval);
    sb.append(", registry=").append(registry);
    sb.append(", options=").append(options);
    sb.append('}');
    return sb.toString();
  }

  private String minifyClassName(String className) {
    return className.replaceAll("\\B\\w+(\\.[a-zA-Z])", "$1");
  }

  private String allPropertiesAsString() {
    Map<String, String> allProperties = new TreeMap<>(options);
    allProperties.put("nThreads", String.valueOf(nThreads));
    allProperties.put("executionTaskDuration", String.valueOf(executionTaskDuration));
    allProperties.put("executionTaskInterval", String.valueOf(executionTaskInterval));
    allProperties.put("numOfIterations", String.valueOf(numOfIterations));
    allProperties.put("rampUpDuration", String.valueOf(rampUpDuration));
    allProperties.put("rampUpInterval", String.valueOf(rampUpInterval));
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : allProperties.entrySet()) {
      sb.append("_").append(entry.getKey()).append("=").append(entry.getValue());
    }
    return sb.toString();
  }

  public static class Builder {
    private final Map<String, String> options = new HashMap<>();

    private int nThreads = N_THREADS;
    private Duration executionTaskDuration = EXECUTION_TASK_DURATION;
    private Duration executionTaskInterval = EXECUTION_TASK_INTERVAL;
    private Duration reporterInterval = REPORTER_INTERVAL;
    private TimeUnit durationUnit = DURATION_UNIT;
    private TimeUnit rateUnit = RATE_UNIT;
    private long numOfIterations = NUM_OF_ITERATIONS;
    private Duration rampUpDuration = RAMP_UP_DURATION;
    private Duration rampUpInterval = RAMP_UP_INTERVAL;

    public Builder from(String[] args) {
      this.parse(args);
      return this;
    }

    private Builder() {}

    public Builder nThreads(int numThreads) {
      this.nThreads = numThreads;
      return this;
    }

    public Builder executionTaskDuration(Duration executionTaskDuration) {
      this.executionTaskDuration = executionTaskDuration;
      return this;
    }

    public Builder executionTaskInterval(Duration executionTaskInterval) {
      this.executionTaskInterval = executionTaskInterval;
      return this;
    }

    public Builder reporterInterval(Duration reporterInterval) {
      this.reporterInterval = reporterInterval;
      return this;
    }

    public Builder addOption(String key, String value) {
      this.options.put(key, value);
      return this;
    }

    public Builder durationUnit(TimeUnit durationUnit) {
      this.durationUnit = durationUnit;
      return this;
    }

    public Builder rateUnit(TimeUnit rateUnit) {
      this.rateUnit = rateUnit;
      return this;
    }

    public Builder numOfIterations(long numOfIterations) {
      this.numOfIterations = numOfIterations;
      return this;
    }

    public Builder rampUpDuration(Duration rampUpDuration) {
      this.rampUpDuration = rampUpDuration;
      return this;
    }

    public Builder rampUpInterval(Duration rampUpInterval) {
      this.rampUpInterval = rampUpInterval;
      return this;
    }

    public BenchmarksSettings build() {
      return new BenchmarksSettings(this);
    }

    private void parse(String[] args) {
      if (args != null) {
        for (String pair : args) {
          String[] keyValue = pair.split("=", 2);
          String key = keyValue[0];
          String value = keyValue[1];
          switch (key) {
            case "nThreads":
              nThreads(Integer.parseInt(value));
              break;
            case "executionTaskDurationInSec":
              executionTaskDuration(Duration.ofSeconds(Long.parseLong(value)));
              break;
            case "executionTaskIntervalInMillis":
              executionTaskInterval(Duration.ofMillis(Long.parseLong(value)));
              break;
            case "reporterIntervalInSec":
              reporterInterval(Duration.ofSeconds(Long.parseLong(value)));
              break;
            case "numOfIterations":
              numOfIterations(Long.parseLong(value));
              break;
            case "rampUpDurationInSec":
              rampUpDuration(Duration.ofSeconds(Long.parseLong(value)));
              break;
            case "rampUpIntervalInMillis":
              rampUpInterval(Duration.ofMillis(Long.parseLong(value)));
              break;
            default:
              addOption(key, value);
              break;
          }
        }
      }
    }
  }
}
