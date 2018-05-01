package aeron.playground;

import io.scalecube.testlib.BaseTest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.Closeable;
import io.rsocket.Frame;
import io.rsocket.Frame.PayloadFrame;
import io.rsocket.FrameType;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.aeron.client.AeronClientTransport;
import io.rsocket.aeron.internal.AeronWrapper;
import io.rsocket.aeron.internal.Constants;
import io.rsocket.aeron.internal.DefaultAeronWrapper;
import io.rsocket.aeron.internal.EventLoop;
import io.rsocket.aeron.internal.SingleThreadedEventLoop;
import io.rsocket.aeron.internal.reactivestreams.AeronClientChannelConnector;
import io.rsocket.aeron.internal.reactivestreams.AeronSocketAddress;
import io.rsocket.aeron.server.AeronServerTransport;
import io.rsocket.util.ByteBufPayload;
import io.rsocket.util.DefaultPayload;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import java.nio.charset.Charset;


public class AeronTransportTest extends BaseTest {
  private static final String GREETING_SAY_HELLO_DATA_JOE = "{'q'=`io.scalecube.services.GreetingService/sayHello`, 'data': 'joe'} ";

  static {
    MediaDriverHolder.getInstance();
    
  }

  public Closeable createServer(String host, int port, SocketAcceptor acceptor) {
    AeronWrapper aeronWrapper = new DefaultAeronWrapper();
    AeronSocketAddress address = AeronSocketAddress.create("aeron:udp", host, port);
    // create server transport;
    EventLoop serverEventLoop = new SingleThreadedEventLoop("server");
    AeronServerTransport server = new AeronServerTransport(aeronWrapper, address, serverEventLoop);

    return RSocketFactory.receive()
        .acceptor(acceptor)
        .transport(server)
        .start()
        .block();

  }

  public RSocket createClient(String host, int port) {
    AeronWrapper aeronWrapper = new DefaultAeronWrapper();
    AeronSocketAddress address = AeronSocketAddress.create("aeron:udp", host, port);
    // Create Client Connector
    EventLoop clientEventLoop = new SingleThreadedEventLoop("client");

    AeronClientChannelConnector.AeronClientConfig config = AeronClientChannelConnector.AeronClientConfig.create(
            address,
            address,
            1,
            2,
            clientEventLoop);


    AeronClientChannelConnector connector =
        AeronClientChannelConnector.create(aeronWrapper, address, clientEventLoop);

    AeronClientTransport client = new AeronClientTransport(connector, config);
    return RSocketFactory.connect()
        .transport(client)
        .start()
        .doOnError(Throwable::printStackTrace)
        .block();
  }

  @Test
  public void clientServerTest() {
    createServer("127.0.0.1", 39790, new TestRSocket());
    RSocket rSocket = createClient("127.0.0.1", 39790);
    
    byte[] hello = (GREETING_SAY_HELLO_DATA_JOE).getBytes(Charset.defaultCharset());
    
    rSocket.fireAndForget( DefaultPayload.create(hello)).block();
        
    System.out.println("done");
  }
}
