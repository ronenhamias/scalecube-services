/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Generated SBE (Simple Binary Encoding) message codec */

package io.rsocket.aeron.internal.reactivestreams.messages;

import org.agrona.MutableDirectBuffer;

@javax.annotation.Generated(
  value = {"io.rsocket.aeron.internal.reactivestreams.messages.MessageHeaderEncoder"}
)
@SuppressWarnings("all")
public class MessageHeaderEncoder {
  public static final int ENCODED_LENGTH = 8;
  private MutableDirectBuffer buffer;
  private int offset;

  public MessageHeaderEncoder wrap(final MutableDirectBuffer buffer, final int offset) {
    this.buffer = buffer;
    this.offset = offset;

    return this;
  }

  public int encodedLength() {
    return ENCODED_LENGTH;
  }

  public static int blockLengthNullValue() {
    return 65535;
  }

  public static int blockLengthMinValue() {
    return 0;
  }

  public static int blockLengthMaxValue() {
    return 65534;
  }

  public MessageHeaderEncoder blockLength(final int value) {
    buffer.putShort(offset + 0, (short) value, java.nio.ByteOrder.LITTLE_ENDIAN);
    return this;
  }

  public static int templateIdNullValue() {
    return 65535;
  }

  public static int templateIdMinValue() {
    return 0;
  }

  public static int templateIdMaxValue() {
    return 65534;
  }

  public MessageHeaderEncoder templateId(final int value) {
    buffer.putShort(offset + 2, (short) value, java.nio.ByteOrder.LITTLE_ENDIAN);
    return this;
  }

  public static int schemaIdNullValue() {
    return 65535;
  }

  public static int schemaIdMinValue() {
    return 0;
  }

  public static int schemaIdMaxValue() {
    return 65534;
  }

  public MessageHeaderEncoder schemaId(final int value) {
    buffer.putShort(offset + 4, (short) value, java.nio.ByteOrder.LITTLE_ENDIAN);
    return this;
  }

  public static int versionNullValue() {
    return 65535;
  }

  public static int versionMinValue() {
    return 0;
  }

  public static int versionMaxValue() {
    return 65534;
  }

  public MessageHeaderEncoder version(final int value) {
    buffer.putShort(offset + 6, (short) value, java.nio.ByteOrder.LITTLE_ENDIAN);
    return this;
  }
}
