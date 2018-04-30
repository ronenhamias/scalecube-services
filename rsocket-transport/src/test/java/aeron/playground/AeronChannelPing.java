package aeron.playground;
/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */



import io.rsocket.aeron.internal.AeronWrapper;
import io.rsocket.aeron.internal.DefaultAeronWrapper;
import io.rsocket.aeron.internal.SingleThreadedEventLoop;
import io.rsocket.aeron.internal.reactivestreams.AeronChannel;
import io.rsocket.aeron.internal.reactivestreams.AeronClientChannelConnector;
import io.rsocket.aeron.internal.reactivestreams.AeronSocketAddress;

import org.agrona.concurrent.UnsafeBuffer;

import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;

/** */
public final class AeronChannelPing {
  private static final String GREETING_SAY_HELLO_DATA_JOE = "{'q'=`io.scalecube.services.GreetingService/sayHello`, 'data': 'joe'} ";

  public static void main(String... args) throws InterruptedException {
    int count = 10_000_000;
    CountDownLatch latch = new CountDownLatch(count);
    AeronWrapper wrapper = new DefaultAeronWrapper();
    AeronSocketAddress managementSocketAddress =
        AeronSocketAddress.create("aeron:udp", "127.0.0.1", 39790);
    SingleThreadedEventLoop eventLoop = new SingleThreadedEventLoop("client");
    AeronClientChannelConnector connector =
        AeronClientChannelConnector.create(wrapper, managementSocketAddress, eventLoop);

    AeronSocketAddress receiveAddress = AeronSocketAddress.create("aeron:udp", "127.0.0.1", 39790);
    AeronSocketAddress sendAddress = AeronSocketAddress.create("aeron:udp", "127.0.0.1", 39790);

    AeronClientChannelConnector.AeronClientConfig config =
        AeronClientChannelConnector.AeronClientConfig.create(
            receiveAddress, sendAddress, 1, 2, eventLoop);

    AeronChannel channel = connector.apply(config).block();

    channel.receive().doOnNext(onNext -> {
      latch.countDown();
      if (latch.getCount() % 100000 == 0) {
        System.out.println(latch.getCount());
      }
    }).subscribe();

    long start = System.currentTimeMillis();
    Flux.range(0, count)
        .flatMap(i -> {
          byte[] hello = (i + GREETING_SAY_HELLO_DATA_JOE).getBytes(Charset.defaultCharset());
          UnsafeBuffer helloBuffer = new UnsafeBuffer(hello);
          return channel.send(helloBuffer);
        }).subscribe();
    
    latch.await(10, TimeUnit.SECONDS);
    System.out.println( System.currentTimeMillis() - start);
    System.out.println("done");
    System.exit(0);
  }
}
