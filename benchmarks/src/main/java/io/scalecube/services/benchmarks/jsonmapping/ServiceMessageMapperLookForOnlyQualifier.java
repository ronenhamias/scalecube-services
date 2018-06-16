package io.scalecube.services.benchmarks.jsonmapping;

import io.scalecube.services.codec.ServiceMessageDataCodec;
import io.scalecube.services.exceptions.BadRequestException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.Charset;

public class ServiceMessageMapperLookForOnlyQualifier {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMessageDataCodec.class);

  private final JsonFactory jsonFactory = new JsonFactory();

  public ServiceMessage2 decode(ByteBuf byteBuf) {
    try (InputStream stream = new ByteBufInputStream(byteBuf.slice())) {
      JsonParser jsonParser = jsonFactory.createParser(stream);

      ServiceMessage2.Builder builder = ServiceMessage2.builder();

      JsonToken current = jsonParser.nextToken();
      if (current != JsonToken.START_OBJECT) {
        System.out.println("Error: root should be object: quiting.");
        LOGGER.error("root should be object: {}", byteBuf.toString(Charset.defaultCharset()));
        throw new BadRequestException("Failed to decode message");
      }

      boolean qualifierNofFound = true;

      while (qualifierNofFound && jsonParser.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = jsonParser.getCurrentName();
        current = jsonParser.nextToken();
        switch (fieldName) {
          case "q":
            builder.qualifier(jsonParser.getValueAsString())
                .dataType(ServiceMessage2.class)
                .data(byteBuf);
            qualifierNofFound = false;
            break;
          case "dataType":
            builder.dataType(jsonParser.getValueAsString());
            break;
          case "data":
            switch (current) {
              case START_OBJECT:
              case START_ARRAY:
                jsonParser.skipChildren();
              case VALUE_STRING:
                builder.dataType(ServiceMessage2.class).data(byteBuf);
                break;
              case VALUE_NULL:// todo ?
              default: // todo nothing (skip) or set data (left only primitives) ?
            }
            break;
          default:
            builder.header(fieldName, jsonParser.getValueAsString());
        }
      }
      return builder.build();
    } catch (Throwable ex) {
      LOGGER.error("Failed to decode message: {}, cause: {}", byteBuf.toString(Charset.defaultCharset()), ex);
      throw new BadRequestException("Failed to decode message");
    } /*
       * finally { ReferenceCountUtil.release(byteBuf); }
       */
  }

  // public ByteBuf encode(ServiceMessage2 message) {
  //
  // }
}
