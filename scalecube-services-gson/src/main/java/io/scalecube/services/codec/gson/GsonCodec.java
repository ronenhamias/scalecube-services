package io.scalecube.services.codec.gson;

import io.scalecube.services.codec.DataCodec;
import io.scalecube.services.codec.HeadersCodec;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class GsonCodec implements DataCodec, HeadersCodec {

  private Gson gson;

  @Override
  public String contentType() {
    return "application/json";
  }

  public GsonCodec() {
    this(initMapper());
  }

  private static Gson initMapper() {
    return new Gson();
  }

  public GsonCodec(Gson mapper) {
    this.gson = mapper;
  }


  @Override
  public void encode(OutputStream out, Map<String, String> headers) throws IOException {
    try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"))) {
      gson.toJson(headers, Map.class, writer);
    }
  }

  @Override
  public Map<String, String> decode(InputStream stream) throws IOException {
    try (JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"))) {
      return stream.available() == 0 ? Collections.emptyMap() : gson.fromJson(reader, Map.class);
    }
  }

  @Override
  public void encode(OutputStream out, Object value) throws IOException {
    try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"))) {
      gson.toJson(value, value.getClass(), writer);
    }
  }

  @Override
  public Object decode(InputStream stream, Class<?> type) throws IOException {
    Objects.requireNonNull(type, "ServiceMessageDataCodecImpl.readFrom requires type is not null");
    try (JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"))) {
      return gson.fromJson(reader, type);
    }
  }
}
