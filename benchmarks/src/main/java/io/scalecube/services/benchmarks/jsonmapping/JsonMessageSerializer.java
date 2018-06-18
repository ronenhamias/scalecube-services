// package io.scalecube.services.benchmarks.jsonmapping;
//
// import java.io.OutputStream;
// import java.nio.charset.Charset;
// import java.util.Map;
//
// import com.fasterxml.jackson.core.JsonEncoding;
// import com.fasterxml.jackson.core.JsonGenerator;
// import com.playtech.openapi.core.codec.IMessageSerializer;
// import com.playtech.openapi.core.messages.Message;
//
// import io.netty.buffer.ByteBuf;
// import io.netty.buffer.ByteBufOutputStream;
// import io.protostuff.JsonIOUtil;
// import io.protostuff.Schema;
// import io.protostuff.runtime.RuntimeSchema;
//
// public final class JsonMessageSerializer implements IMessageSerializer {
//
// @Override
// public void serialize(Message message, ByteBuf bb) throws Exception {
// JsonGenerator generator = JsonIOUtil.DEFAULT_JSON_FACTORY.createGenerator(
// (OutputStream) new ByteBufOutputStream(bb), JsonEncoding.UTF8);
// generator.writeStartObject();
//
// // generic message headers
// if (message.getQualifier() != null) {
// generator.writeStringField(Message.QUALIFIER_NAME, message.getQualifier());
// }
// if (message.getCorrelationId() != null) {
// generator.writeStringField(Message.CORRELATION_ID_NAME, message.getCorrelationId());
// }
// if (message.getContextId() != null) {
// generator.writeStringField(Message.CONTEXT_ID_NAME, message.getContextId());
// }
// if (message.getSubscriptionId() != null) {
// generator.writeStringField(Message.SUBSCRIPTION_ID_NAME, message.getSubscriptionId());
// }
//
// // authentication headers
// if (message.getAuthType() != null) {
// generator.writeStringField(Message.AUTH_TYPE_NAME, message.getAuthType());
// }
// if (message.getAuthCredentials() != null) {
// generator.writeStringField(Message.AUTH_CREDENTIALS_NAME, message.getAuthCredentials());
// }
// if (message.getUserAttributes() != null) {
// generator.writeFieldName(Message.USER_ATTRIBUTES_NAME);
// generator.writeStartObject();
// for (Map.Entry<String, String> entry : message.getUserAttributes().entrySet()) {
// generator.writeStringField(entry.getKey(), entry.getValue());
// }
// generator.writeEndObject();
// }
//
// // data
// Object data = message.getData();
// if (data != null) {
// if (data instanceof byte[]) {
// generator.writeFieldName(Message.DATA_NAME);
// generator.writeRawValue(new String((byte[]) data, Charset.forName("UTF-8")));
// } else if (data instanceof String) {
// generator.writeFieldName(Message.DATA_NAME);
// generator.writeRawValue((String) data);
// } else if (data instanceof ByteBuf) {
// ByteBuf dataBin = (ByteBuf) data;
// if (dataBin.readableBytes() > 0) {
// generator.writeFieldName(Message.DATA_NAME);
// // dataBin is certainly valid JSON object
// generator.writeRaw(":");
// generator.flush();
// bb.writeBytes(dataBin);
// }
// } else {
// generator.writeFieldName(Message.DATA_NAME);
// Schema schema = RuntimeSchema.getSchema(data.getClass());
// JsonIOUtil.writeTo(generator, data, schema, false);
// }
// }
//
// generator.writeEndObject();
// generator.close();
// }
// }
