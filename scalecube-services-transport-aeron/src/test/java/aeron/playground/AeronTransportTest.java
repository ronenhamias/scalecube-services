package aeron.playground;

import io.scalecube.testlib.BaseTest;

import io.aeron.driver.MediaDriver;
import io.rsocket.Closeable;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.aeron.client.AeronClientTransport;
import io.rsocket.aeron.internal.AeronWrapper;
import io.rsocket.aeron.internal.DefaultAeronWrapper;
import io.rsocket.aeron.internal.EventLoop;
import io.rsocket.aeron.internal.SingleThreadedEventLoop;
import io.rsocket.aeron.internal.reactivestreams.AeronClientChannelConnector;
import io.rsocket.aeron.internal.reactivestreams.AeronClientChannelConnector.AeronClientConfig;
import io.rsocket.aeron.internal.reactivestreams.AeronSocketAddress;
import io.rsocket.aeron.server.AeronServerTransport;
import io.rsocket.util.DefaultPayload;

import org.junit.Test;

import java.nio.charset.Charset;


public class AeronTransportTest extends BaseTest {
  private static final String GREETING_SAY_HELLO_DATA_JOE = "{'q'=`io.scalecube.services.GreetingService/sayHello`, 'data': 'joe'} ";
  private MediaDriver driver = MediaDriverBuilder.create("aeron/media/1.9.3");
  
  public Closeable createServer(String host, int port, SocketAcceptor acceptor) {
    
    AeronWrapper aeronWrapper = new DefaultAeronWrapper(driver.aeronDirectoryName());
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
    AeronWrapper aeronWrapper = new DefaultAeronWrapper(driver.aeronDirectoryName());
    SingleThreadedEventLoop eventLoop = new SingleThreadedEventLoop("client");
  
    AeronSocketAddress receiveAddress = AeronSocketAddress.create("aeron:udp", host, port);
    AeronSocketAddress sendAddress = AeronSocketAddress.create("aeron:udp", host, port);

    AeronClientConfig config = AeronClientConfig.create(sendAddress, receiveAddress, 1, 2, eventLoop);
    AeronClientChannelConnector connector =
        AeronClientChannelConnector.create(aeronWrapper, sendAddress, eventLoop);

    AeronClientTransport client = new AeronClientTransport(connector, config);
    return RSocketFactory.connect()
        .transport(client)
        .start()
        .doOnError(Throwable::printStackTrace).block();
  }

  @Test
  public void clientServerTest() {
    createServer("127.0.0.1", 4000, new TestRSocket());
    RSocket rSocket = createClient("127.0.0.1", 4000);
    
    byte[] hello = (GREETING_SAY_HELLO_DATA_JOE).getBytes(Charset.defaultCharset());
    
    rSocket.requestResponse(DefaultPayload.create(hello)).subscribe();
        
    System.out.println("done");
  }
}
