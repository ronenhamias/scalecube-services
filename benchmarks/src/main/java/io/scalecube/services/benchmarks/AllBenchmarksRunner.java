package io.scalecube.services.benchmarks;

public class AllBenchmarksRunner {

  public static final String EXEC_TIME_ARG = "executionTaskTimeInSec=%d";
  public static final Integer EXEC_TIME = 180;

  public static void main(String ... args) {
    String execTimeArg = String.format(EXEC_TIME_ARG, EXEC_TIME);
    OneWayBenchmarksRunner.main(execTimeArg);
    OneWayCallBenchmarksRunner.main(execTimeArg);
    RequestManyBenchmarksRunner.main(execTimeArg);
    RequestManyCallBenchmarksRunner.main(execTimeArg);
    RequestManyLatencyBenchmarksRunner.main(execTimeArg);
    RequestOneBenchmarksRunner.main(execTimeArg);
    RequestOneCallBenchmarksRunner.main(execTimeArg);
    RouterBenchmarksRunner.main(execTimeArg);
  }
}