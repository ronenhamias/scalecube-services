package aeron.playground;

import io.scalecube.testlib.BaseTest;

import io.rsocket.aeron.internal.reactivestreams.AeronChannelServer.AeronChannelStartedServer;

import org.junit.Test;


public class AeronTest extends BaseTest {

  @Test
  public void test_encode_decode_ServiceMessage_success() throws InterruptedException {

    AeronChannelStartedServer start = AeronChannelPongServer.main(new String[] {});
    AeronChannelPing.main(new String[] {});
    start.awaitShutdown();
  }
  
}

