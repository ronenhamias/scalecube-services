package io.scalecube.services.benchmarks.jsonmapping;

import static com.google.common.base.Preconditions.checkState;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ByteProcessor;
import io.netty.util.Recycler;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

final class OpenAPIJsonParser {

  static final byte ASCII_COLON = 58; // :
  static final byte ASCII_DOUBLE_QUOTES = 34; // "
  static final byte ASCII_OPENING_BRACE = 123; // {
  static final byte ASCII_CLOSING_BRACE = 125; // }
  static final byte ASCII_COMMA = 44; // ,
  static final byte ASCII_ESCAPE = 92; // \

  // Also allowed to be escaped in string vvalues
  static final byte ASCII_SLASH = 47; // / (solidus)
  static final byte ASCII_BACKSPACE = 8; // backspace
  static final byte ASCII_FORM_FEED = 12; // form feed

  // Insignificant whitespaces
  static final byte ASCII_WHITE_SPACE = 32; // Space
  static final byte ASCII_HORIZONTAL_TAB = 9; // Horizontal tab
  static final byte ASCII_NEW_LINE = 10; // Line feed or New line
  static final byte ASCII_CR = 13; // Carriage return
  static final byte ASCII_U_HEX = 117; // Escape any hex symbol

  /**
   * @param bb original socket channel byte_buffer.
   * @param headersToGet collection of header names for getting values for (string objects will be allocated).
   * @param headersToMatch collection of header names to match (sliced buffers will be created).
   * @param handler a handler {@link com.google.common.base.Function}, argument always has two elements:
   *        {@code [header_name, {header_value|ByteBuf}]}.
   */
  public void parse(ByteBuf bb, Collection<String> headersToGet, Collection<String> headersToMatch,
      Function<Object[], Void> handler) {
    List<String> h_get = new ArrayList<>(headersToGet);
    List<String> h_match = new ArrayList<>(headersToMatch);

    boolean seekingNextObj = false;
    int l_quotes = -1;
    int r_quotes = -1;
    while (bb.readableBytes() > 0) {
      if (h_get.isEmpty() && h_match.isEmpty())
        return;

      byte c = bb.readByte();
      if (seekingNextObj) {
        if (!isSignificantSymbol(c))
          continue;
        checkState(c == ASCII_COMMA || c == ASCII_CLOSING_BRACE, "Invalid JSON request");
        seekingNextObj = false;
      }
      if (c == ASCII_DOUBLE_QUOTES) { // match ASCII double quotes
        if (l_quotes == -1)
          l_quotes = bb.readerIndex();
        else
          r_quotes = bb.readerIndex() - 1;
        continue;
      }

      if (l_quotes != -1 && r_quotes != -1) { // if two double quotes were matched -- match following colon
        if (!isSignificantSymbol(c))
          continue;

        if (c == ASCII_COLON) {
          ByteBuf bb_slice = bb.slice(l_quotes, r_quotes - l_quotes);
          int i;
          if ((i = headerInd(h_get, bb_slice)) >= 0) {
            Object[] arg = new Object[2];
            String headerName = h_get.remove(i);
            arg[0] = headerName;
            arg[1] = getFlatHeaderValue(bb, headerName);
            handler.apply(arg);
          } else if ((i = headerInd(h_match, bb_slice)) >= 0) {
            Object[] arg = new Object[2];
            String headerName = h_match.remove(i);
            arg[0] = headerName;
            arg[1] = matchHeaderValue(bb, headerName);
            handler.apply(arg);
          } else {
            skipValue(bb);
          }
        }
        seekingNextObj = true;
        l_quotes = -1;
        r_quotes = -1;
      }
    }

  }

  private static int[] skipValue(ByteBuf bb) {
    int[] result = {-1, -1};
    MatchHeaderByteBufProcessor processor = MatchHeaderByteBufProcessor.newInstance(bb.readerIndex());
    try {
      bb.forEachByte(processor);
      checkState(processor.bracesCounter == 0, "Invalid JSON. Curly braces are incorrect!");
      bb.readerIndex(processor.index);
      result[0] = processor.l_braces;
      result[1] = processor.index - processor.l_braces;
    } finally {
      processor.recycle();
    }
    return result;
  }

  private static int headerInd(List<String> headers, ByteBuf bb_slice) {
    for (int i = 0; i < headers.size(); i++) {
      if (headerMatched(headers.get(i), bb_slice)) {
        return i;
      }
    }
    return -1;
  }

  private static boolean headerMatched(String header, ByteBuf bb_slice) {
    boolean matched = bb_slice.capacity() == header.length();
    if (matched) {
      StringMatchProcessor processor = StringMatchProcessor.newInstance(header);
      try {
        if (bb_slice.forEachByte(processor) > -1) {
          return false;
        }
      } finally {
        processor.recycle();
      }
    }
    return matched;
  }

  /**
   * @return allocated utf-8 String object.
   */
  private static String getFlatHeaderValue(ByteBuf bb, String headerName) {
    GetHeaderProcessor processor = GetHeaderProcessor.newInstance(bb.readerIndex());
    String result = null;
    try {
      bb.forEachByte(processor);
      checkState(processor.firstQuotes != -1,
          "Invalid JSON. Can't find value opening quotes! headerName=" + headerName);
      checkState(processor.firstQuotes != processor.quotes,
          "Invalid JSON. Value closing quotes are missing! headerName="
              + headerName);
      bb.readerIndex(processor.readerIndex);
      result = processor.targetBuf.toString(Charset.forName("UTF-8"));
    } finally {
      if (processor != null) {
        processor.recycle();
      }
    }
    return result;
  }

  /**
   * @return sliced ByteBuf object encapsulated header value.
   */
  private static ByteBuf matchHeaderValue(ByteBuf bb, String headerName) {
    int[] matchedValueBounds = skipValue(bb);
    checkState(matchedValueBounds[1] > -1, "InvalidJSON. Header : " + headerName);
    return bb.slice(matchedValueBounds[0], matchedValueBounds[1]);
  }

  /**
   * Check if the character with specified code (ASCII) is allowed to be escaped inside JSON string
   */
  private static boolean mightBeEscaped(byte character) {
    // TODO: currently - do not allow escaping of sequence like "\uABCD", which is possible for JSON
    return character == ASCII_DOUBLE_QUOTES ||
        character == ASCII_ESCAPE ||
        character == ASCII_SLASH ||
        character == ASCII_BACKSPACE ||
        character == ASCII_FORM_FEED ||
        character == ASCII_NEW_LINE ||
        character == ASCII_CR ||
        character == ASCII_U_HEX ||
        character == ASCII_HORIZONTAL_TAB;
  }

  /**
   *
   * @param b - code of symbol to check
   * @return {@code true} if the symbol with code {@code b} (utf-8) is significant according to JSON spec
   */
  private static boolean isSignificantSymbol(byte b) {
    return b != ASCII_WHITE_SPACE &&
        b != ASCII_HORIZONTAL_TAB &&
        b != ASCII_NEW_LINE &&
        b != ASCII_CR;
  }

  static class MatchHeaderByteBufProcessor implements ByteProcessor {
    private static final Recycler<MatchHeaderByteBufProcessor> RECYCLER = new Recycler<MatchHeaderByteBufProcessor>() {
      @Override
      protected MatchHeaderByteBufProcessor newObject(Handle<MatchHeaderByteBufProcessor> handle) {
        return new MatchHeaderByteBufProcessor(handle);
      }
    };
    static final int STATE_START = 1;
    static final int STATE_OBJECT = 2;
    static final int STATE_ESCAPED = 4;
    static final int STATE_STRING = 8;
    int l_braces = -1;
    int bracesCounter = 0;
    int index = 0;
    int state = STATE_START;
    private final Recycler.Handle<MatchHeaderByteBufProcessor> handle;

    public MatchHeaderByteBufProcessor(Recycler.Handle<MatchHeaderByteBufProcessor> handle) {
      this.handle = handle;
    }

    public static MatchHeaderByteBufProcessor newInstance(int readerIndex) {
      MatchHeaderByteBufProcessor matchHeaderByteBufProcessor = RECYCLER.get();
      matchHeaderByteBufProcessor.index = readerIndex;
      return matchHeaderByteBufProcessor;
    }

    public void recycle() {
      state = STATE_START;
      index = 0;
      l_braces = -1;
      bracesCounter = -1;
      handle.recycle(this);
    }

    @Override
    public boolean process(byte value) throws Exception {
      if ((state & STATE_ESCAPED) != 0) {
        state ^= STATE_ESCAPED;
        index++;
        return true;
      }
      if ((state & STATE_START) != 0 && value == ASCII_OPENING_BRACE) { // match first opening curly brace
        bracesCounter = 1;
        l_braces = index;
        state = STATE_OBJECT;
      } else if ((state & STATE_START) != 0 && value == ASCII_DOUBLE_QUOTES) {
        bracesCounter = 1;
        l_braces = index;
        state = STATE_STRING;
      } else {
        boolean isNotString = (state & STATE_STRING) == 0;
        if (value == ASCII_ESCAPE) {
          checkState(!isNotString, "Invalid JSON. Escape symbol present outside of string");
          state |= STATE_ESCAPED;
          index++;
          return true;
        }
        if (value == ASCII_DOUBLE_QUOTES) {
          if ((state & STATE_OBJECT) != 0) {
            state ^= STATE_STRING;
          } else if (state == STATE_STRING) {
            bracesCounter--;
          }
        }
        if (value == ASCII_OPENING_BRACE && isNotString) {
          bracesCounter++;
        } else if (value == ASCII_CLOSING_BRACE && isNotString) {
          bracesCounter--;
        }
      }
      index++;
      // should return false if processing is finished. Return false only when inside object and braces counter is equal
      // 0
      boolean terminateCondition = (state == STATE_OBJECT || state == STATE_STRING) && bracesCounter == 0;

      return !terminateCondition;
    }
  }

  static class StringMatchProcessor implements ByteProcessor {

    private static final Recycler<StringMatchProcessor> RECYCLER = new Recycler<StringMatchProcessor>() {
      @Override
      protected StringMatchProcessor newObject(Handle<StringMatchProcessor> handle) {
        return new StringMatchProcessor(handle);
      }
    };
    private String stringToMatch;
    private int index;
    private final Recycler.Handle<StringMatchProcessor> handle;

    StringMatchProcessor(Recycler.Handle<StringMatchProcessor> handle) {
      this.handle = handle;
    }

    public static StringMatchProcessor newInstance(String stringToMatch) {
      StringMatchProcessor stringMatchProcessor = RECYCLER.get();
      stringMatchProcessor.stringToMatch = stringToMatch;
      stringMatchProcessor.index = 0;
      return stringMatchProcessor;
    }

    public void recycle() {
      handle.recycle(this);
    }

    @Override
    public boolean process(byte value) throws Exception {
      return value == stringToMatch.charAt(index++);
    }
  }

  static class GetHeaderProcessor implements ByteProcessor {

    private static final Recycler<GetHeaderProcessor> RECYCLER = new Recycler<GetHeaderProcessor>() {
      @Override
      protected GetHeaderProcessor newObject(Handle<GetHeaderProcessor> handle) {
        return new GetHeaderProcessor(handle);
      }
    };

    public final Recycler.Handle<GetHeaderProcessor> handle;
    int firstQuotes = -1;
    int quotes = -1;
    boolean inEscapeState = false;
    boolean isParsingHex = false;
    byte[] parsingHex = new byte[4];
    boolean isParsingString = false;
    int escapedHexCounter = 0;
    ByteBuf targetBuf;
    int readerIndex;

    GetHeaderProcessor(Recycler.Handle<GetHeaderProcessor> handle) {
      this.handle = handle;
    }

    public static GetHeaderProcessor newInstance(int readerIndex) {
      GetHeaderProcessor getHeaderProcessor = RECYCLER.get();
      getHeaderProcessor.readerIndex = readerIndex;
      getHeaderProcessor.firstQuotes = -1;
      getHeaderProcessor.quotes = -1;
      getHeaderProcessor.inEscapeState = false;
      getHeaderProcessor.isParsingString = false;
      getHeaderProcessor.isParsingHex = false;
      getHeaderProcessor.escapedHexCounter = 0;
      getHeaderProcessor.targetBuf = PooledByteBufAllocator.DEFAULT.buffer();
      return getHeaderProcessor;
    }

    public void recycle() {
      targetBuf.release();
      handle.recycle(this);
    }

    @Override
    public boolean process(byte c) throws Exception {
      readerIndex++; // increase every iteration, use (reader - 1) value
      // skip insignificant chars before quotes
      if (!isParsingString && !isSignificantSymbol(c)) {
        return true;
      }
      // consume escaped symbol
      if (c == ASCII_ESCAPE && !inEscapeState) {
        checkState(isParsingString, "Invalid JSON. Got '\\' before first opening quotes!");
        inEscapeState = true;
        return true; // ignore rest loop block
      }
      if (c == ASCII_DOUBLE_QUOTES && !inEscapeState) { // match non-escaped ASCII double double quotes
        quotes = readerIndex - 1; // .. always track last non-escaped double quote
        if (!isParsingString) {
          firstQuotes = quotes; // save first double quotes
          isParsingString = true;
          return true;
        } else {
          return false; // terminate the parsing since the string is over
        }
      }
      // check if the character might be escaped and resed 'escaped' state
      if (inEscapeState) {
        checkState(mightBeEscaped(c), "Invalid JSON. The character " + c + "might not be escaped.");
        if (c == ASCII_U_HEX) {
          isParsingHex = true;
          inEscapeState = false;
          return true;
        }
        inEscapeState = false;
      }

      // parse hex (should be at bottom, since modifying char of result)
      if (isParsingHex) {
        checkState((c >= 48 && c <= 57) || (c >= 65 && c <= 70) || (c >= 97 && c <= 102)); // check if char is hex
                                                                                           // symbol [0-1][A-F]
        parsingHex[escapedHexCounter++] = c;
        if (escapedHexCounter == 4) {
          isParsingHex = false; // not parsing \\uXXXX anymore

          int i = Integer.parseInt(new String(parsingHex, "UTF-8"), 16);
          if (i < 0x80) { // 1 byte
            targetBuf.writeByte(i);
          } else if (i < 0x800) { // 2 bytes
            targetBuf.writeByte(0xc0 | (i >> 6));
            targetBuf.writeByte(0x80 | (i & 0x3f));
          } else if (i < 0xd800) { // 3 bytes
            targetBuf.writeByte(0xe0 | (i >> 12));
            targetBuf.writeByte(0x80 | ((i >> 6) & 0x3f));
            targetBuf.writeByte(0x80 | (i & 0x3f));
          } else {
            throw new IllegalStateException("Surrogate symbols are not supported");
          }
          escapedHexCounter = 0;
        }
        return true;
      }
      targetBuf.writeByte(c);
      return true;
    }
  }
}
