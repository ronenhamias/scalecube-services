package io.scalecube.services.codec.dsljson;

import io.scalecube.services.codec.DataCodec;
import io.scalecube.services.codec.HeadersCodec;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

public final class DslJsonCodec implements DataCodec, HeadersCodec {

  // private final TypeReference<Map<String, String>> mapType = new TypeReference<Map<String, String>>() {};

  private final DslJson<Object> dsljson;

  public DslJsonCodec() {
    this(new DslJson<>(Settings.withRuntime().includeServiceLoader()));
  }

  public DslJsonCodec(DslJson<Object> dsljson) {
    this.dsljson = dsljson;
  }

  @Override
  public String contentType() {
    return "application/json";
  }

  @Override
  public void encode(OutputStream stream, Map<String, String> headers) throws IOException {
    dsljson.serialize(headers, stream);
  }

  @Override
  public Map<String, String> decode(InputStream stream) throws IOException {
    return stream.available() == 0 ? Collections.emptyMap() : dsljson.deserialize(Map.class, stream);
  }

  @Override
  public void encode(OutputStream stream, Object value) throws IOException {
    dsljson.serialize(value, stream);
  }

  @Override
  public Object decode(InputStream stream, Class<?> type) throws IOException {
    return dsljson.deserialize(type, stream);
  }
}
