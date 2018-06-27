package io.scalecube.services.benchmarks.jsonmapping;

import com.google.common.collect.ImmutableList;

import io.netty.buffer.ByteBuf;

import java.util.List;

public final class SelfWrittenJsonMessageDeserializer {

  private static final SelfWrittenJsonParser jsonParser = new SelfWrittenJsonParser();

  private static final List<String> get_headers = ImmutableList.of("q", "dataType", "unknown");
  private static final List<String> match_headers = ImmutableList.of("data");

  public FlatServiceMessage deserialize(ByteBuf bb) {
    final FlatServiceMessage.Builder messageBuilder = FlatServiceMessage.builder();
    jsonParser.parse(bb.slice(), get_headers, match_headers, input -> {
      switch ((String) input[0]) {
        case "q":
          messageBuilder.qualifier((String) input[1]);
          break;
        case "dataType":
          messageBuilder.dataType((String) input[1]);
          break;
        case "unknown":
          messageBuilder.header((String) input[0], (String) input[1]);
          break;
        case "data":
          messageBuilder.data(bb);
          break;
      }
      return null;
    });

    return messageBuilder.build();
  }
}
