package io.scalecube.services.benchmarks;

import io.scalecube.benchmarks.BenchmarksSettings;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.codec.ServiceMessageCodec;

import com.codahale.metrics.Timer;

import io.rsocket.Payload;

public class DecodeServiceMessageCodecBenchmarksRunner {

  public static void main(String[] args) {
    BenchmarksSettings settings = BenchmarksSettings.from(args).build();
    new ServiceMessageCodecBenchmarkState(settings).runForSync(state -> {

      Timer timer = state.timer("timer");
      ServiceMessageCodec codec = state.codec();
      Payload payloadMessage = state.payloadMessage();

      return i -> {
        Timer.Context timeContext = timer.time();
        ServiceMessage msg = codec.decode(payloadMessage.sliceData().retain(), payloadMessage.sliceMetadata().retain());
        timeContext.stop();
        return msg;
      };
    });
  }
}
