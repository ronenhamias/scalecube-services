package io.scalecube.services.codec.dsljson;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.runtime.Settings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class Test {

  private static final DslJson<Object> dsljson = new DslJson<>(Settings.withRuntime().includeServiceLoader());

  public static void main(String[] args) throws IOException {
    JsonWriter jsonWriter = dsljson.newWriter();

    SomeEntity someEntity = new SomeEntity();
    someEntity.setCheck(true); // only getter, isProperty doesn't work
    someEntity.setCheck2(true);
    someEntity.setId("asfsqw342324");
    someEntity.setCount(213);
    someEntity.setPrice(2134454364235L);
    someEntity.setDuration(Duration.ofSeconds(221)); // doesn't work
    someEntity.setLocalDateTime(LocalDateTime.now());

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    dsljson.serialize(someEntity, os);
    System.out.println(os.toString());

    dsljson.serialize(jsonWriter, someEntity);

    String raw = jsonWriter.toString();
    System.out.println(raw);


    ByteArrayInputStream is = new ByteArrayInputStream(raw.getBytes());

    SomeEntity after = dsljson.deserialize(SomeEntity.class, is);

    System.out.println("after = " + after);
  }
}
