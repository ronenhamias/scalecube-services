package io.scalecube.services.benchmarks.jsonmapping;

import com.google.common.collect.ImmutableList;

import io.netty.buffer.ByteBuf;

import java.util.List;

public final class JsonFlatMessageDeserializer {

  private static final OpenAPIJsonParser jsonParser = new OpenAPIJsonParser();

  private static final List<String> get_headers = ImmutableList.of("q", "dataType", "unknown");
  private static final List<String> match_headers = ImmutableList.of("data");

  public ServiceMessage2 deserialize(ByteBuf bb) {
    final ServiceMessage2.Builder messageBuilder = ServiceMessage2.builder();
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
        // case CONTEXT_ID_NAME:
        // messageBuilder.contextId((String) input[1]);
        // break;
        // case SUBSCRIPTION_ID_NAME:
        // messageBuilder.subscriptionId((String) input[1]);
        // break;
        // case AUTH_TYPE_NAME:
        // messageBuilder.authType((String) input[1]);
        // break;
        // case AUTH_CREDENTIALS_NAME:
        // messageBuilder.authCredentials((String) input[1]);
        // break;
        // case USER_ATTRIBUTES_NAME:
        // Map<String, String> userAttributes = null;
        // try {
        // //noinspection unchecked
        // userAttributes = mapper.readValue((InputStream) new ByteBufInputStream((ByteBuf) input[1]), Map.class);
        // } catch (IOException e) {
        // LOGGER.warn("Failed to deserialize userAttributes: " + e, e);
        // }
        // messageBuilder.userAttributes(userAttributes);
        // break;
        case "data":
          messageBuilder.data(bb);
          break;
      }
      return null;
    });

    return messageBuilder.build();
  }
}
