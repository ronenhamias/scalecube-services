package io.scalecube.services.benchmarks.jsonmapping;

import io.scalecube.services.codec.ServiceMessageDataCodec;
import io.scalecube.services.exceptions.BadRequestException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class SimpleServiceMessageObjectMapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMessageDataCodec.class);

  private final ObjectMapper mapper;

  public SimpleServiceMessageObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
    mapper.registerModule(new JavaTimeModule());
    this.mapper = mapper;
  }

  public FlatServiceMessage decode(ByteBuf byteBuf) {
    try (InputStream stream = new ByteBufInputStream(byteBuf.slice())) {

      FlatServiceMessage.Builder builder = FlatServiceMessage.builder();

      Map map = mapper.readValue(stream, HashMap.class);

      if (map.containsKey("q")) {
        builder.qualifier(map.remove("q").toString());
      }
      if (map.containsKey("dataType")) {
        builder.dataType(map.remove("dataType").toString());
      }
      if (map.containsKey("data")) {
        builder.data(map.remove("data"));
      }
      return builder.headers(map).build();
    } catch (Throwable ex) {
      LOGGER.error("Failed to decode message: {}, cause: {}", byteBuf.toString(Charset.defaultCharset()), ex);
      throw new BadRequestException("Failed to decode message");
    } /*
       * finally { ReferenceCountUtil.release(byteBuf); }
       */
  }

  // public ByteBuf encode(FlatServiceMessage message) {
  //
  // }
}
