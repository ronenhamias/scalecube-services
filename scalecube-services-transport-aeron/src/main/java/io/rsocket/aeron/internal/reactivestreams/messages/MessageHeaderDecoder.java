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

import org.agrona.DirectBuffer;

@javax.annotation.Generated(
  value = {"io.rsocket.aeron.internal.reactivestreams.messages.MessageHeaderDecoder"}
)
@SuppressWarnings("all")
public class MessageHeaderDecoder {
  public static final int ENCODED_LENGTH = 8;
  private DirectBuffer buffer;
  private int offset;

  public MessageHeaderDecoder wrap(final DirectBuffer buffer, final int offset) {
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

  public int blockLength() {
    return (buffer.getShort(offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF);
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

  public int templateId() {
    return (buffer.getShort(offset + 2, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF);
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

  public int schemaId() {
    return (buffer.getShort(offset + 4, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF);
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

  public int version() {
    return (buffer.getShort(offset + 6, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF);
  }
}
