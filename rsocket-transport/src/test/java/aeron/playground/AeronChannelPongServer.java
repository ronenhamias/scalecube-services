package aeron.playground;
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



import io.rsocket.aeron.internal.AeronWrapper;
import io.rsocket.aeron.internal.DefaultAeronWrapper;
import io.rsocket.aeron.internal.SingleThreadedEventLoop;
import io.rsocket.aeron.internal.reactivestreams.AeronChannelServer;
import io.rsocket.aeron.internal.reactivestreams.AeronSocketAddress;

import org.agrona.concurrent.UnsafeBuffer;

/** */
public class AeronChannelPongServer {
  private static final String GREETING_SAY_HELLO_DATA_JOE = "{'q'=`io.scalecube.services.GreetingService/sayHello`, 'data': 'joe'} ";
  
  public static void main(String... args) {
    MediaDriverHolder holder = MediaDriverHolder.getInstance();
    
    AeronWrapper wrapper = new DefaultAeronWrapper();
    AeronSocketAddress managementSubscription =
        AeronSocketAddress.create("aeron:udp", "127.0.0.1", 39790);
    SingleThreadedEventLoop eventLoop = new SingleThreadedEventLoop("server");

    AeronChannelServer.AeronChannelConsumer consumer =
        aeronChannel -> {
           aeronChannel.receive().subscribe(c ->{
             byte[] hello = GREETING_SAY_HELLO_DATA_JOE.getBytes();
             UnsafeBuffer helloBuffer = new UnsafeBuffer(hello);     
             aeronChannel.send(helloBuffer).doOnError(Throwable::printStackTrace).subscribe();
           });
         
        };

    AeronChannelServer server =
        AeronChannelServer.create(consumer, wrapper, managementSubscription, eventLoop);
    AeronChannelServer.AeronChannelStartedServer start = server.start();
    start.awaitShutdown();
  }
}
