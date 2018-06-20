package io.scalecube.services.codec;

import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.exceptions.BadRequestException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public final class ServiceMessageCodec {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMessageCodec.class);

  private static final String DEFAULT_DATA_FORMAT = "application/json";

  private static final String HEADER_QUALIFIER = "q";
  private static final String HEADER_STREAM_ID = "sid";
  private static final String HEADER_DATA_TYPE = "_type";
  private static final String HEADER_DATA_FORMAT = "_data_format";
  private static final String HEADER_INACTIVITY = "_inactivity";

  private final HeadersCodec headersCodec;

  public ServiceMessageCodec(HeadersCodec headersCodec) {
    this.headersCodec = headersCodec;
  }

  public <T> T encodeAndTransform(ServiceMessage message, BiFunction<ByteBuf, ByteBuf, T> transformer) {
    ByteBuf dataBuffer = Unpooled.EMPTY_BUFFER;
    ByteBuf headersBuffer = Unpooled.EMPTY_BUFFER;

    if (message.hasData(ByteBuf.class)) {
      dataBuffer = message.data();
    } else if (message.hasData()) {
      dataBuffer = ByteBufAllocator.DEFAULT.buffer();
      try {
        String contentType = Optional.ofNullable(message.dataFormat()).orElse(DEFAULT_DATA_FORMAT);
        DataCodec dataCodec = DataCodec.getInstance(contentType);
        dataCodec.encode(new ByteBufOutputStream(dataBuffer), message.data());
      } catch (Throwable ex) {
        ReferenceCountUtil.release(dataBuffer);
        LOGGER.error("Failed to encode data on: {}, cause: {}", message, ex);
        throw new BadRequestException("Failed to encode data on message q=" + message.qualifier());
      }
    }

    Map<String, String> headers = new HashMap<>(message.headers());
    if (message.qualifier() != null) {
      headers.put(HEADER_QUALIFIER, message.qualifier());
    }
    if (message.streamId() != null) {
      headers.put(HEADER_STREAM_ID, message.streamId());
    }
    if (message.dataType() != null) {
      headers.put(HEADER_DATA_TYPE, message.dataType());
    }
    if (message.dataFormat() != null) {
      headers.put(HEADER_DATA_FORMAT, message.dataFormat());
    }
    if (message.inactivity() != null) {
      headers.put(HEADER_INACTIVITY, message.inactivity());
    }

    if (!headers.isEmpty()) { // todo maybe it is unnecessary to check
      headersBuffer = ByteBufAllocator.DEFAULT.buffer();
      try {
        headersCodec.encode(new ByteBufOutputStream(headersBuffer), headers);
      } catch (Throwable ex) {
        ReferenceCountUtil.release(headersBuffer);
        LOGGER.error("Failed to encode headers on: {}, cause: {}", message, ex);
        throw new BadRequestException("Failed to encode headers on message q=" + message.qualifier());
      }
    }

    return transformer.apply(dataBuffer, headersBuffer);
  }

  public ServiceMessage decode(ByteBuf dataBuffer, ByteBuf headersBuffer) {
    ServiceMessage.Builder builder = ServiceMessage.builder();
    if (dataBuffer.isReadable()) {
      builder.data(dataBuffer);
    }
    if (headersBuffer.isReadable()) {
      try (ByteBufInputStream stream = new ByteBufInputStream(headersBuffer.slice())) {
        Map<String, String> headers = headersCodec.decode(stream);
        builder
            .qualifier(headers.remove(HEADER_QUALIFIER))
            .streamId(headers.remove(HEADER_STREAM_ID))
            .dataType(headers.remove(HEADER_DATA_TYPE))
            .dataFormat(headers.remove(HEADER_DATA_FORMAT))
            .inactivity(headers.remove(HEADER_INACTIVITY))
            .headers(headers);
      } catch (Throwable ex) {
        LOGGER.error("Failed to decode message headers: {}, cause: {}",
            headersBuffer.toString(Charset.defaultCharset()), ex);
        throw new BadRequestException("Failed to decode message headers {headers=" + headersBuffer.readableBytes()
            + ", data=" + dataBuffer.readableBytes() + "}");
      } finally {
        ReferenceCountUtil.release(headersBuffer);
      }
    }
    return builder.build();
  }
}
