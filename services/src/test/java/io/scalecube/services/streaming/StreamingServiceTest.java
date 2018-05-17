package io.scalecube.services.streaming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.scalecube.cluster.membership.MembershipEvent;
import io.scalecube.services.BaseTest;
import io.scalecube.services.Messages;
import io.scalecube.services.Microservices;
import io.scalecube.services.ServiceCall.Call;
import io.scalecube.services.api.ServiceMessage;

import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

public class StreamingServiceTest extends BaseTest {

  private MetricRegistry registry = new MetricRegistry();
  private static AtomicInteger port = new AtomicInteger(6000);

  @Test
  public void test_quotes() throws InterruptedException {
    QuoteService service = new SimpleQuoteService();
    CountDownLatch latch = new CountDownLatch(3);
    Disposable sub = service.quotes().subscribe(onNext -> {
      System.out.println("test_quotes: " + onNext);
      latch.countDown();
    });
    latch.await(4, TimeUnit.SECONDS);
    sub.dispose();
    assertTrue(latch.getCount() == 0);
  }

  @Test
  public void test_local_quotes_service() throws InterruptedException {
    Microservices node = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .services(new SimpleQuoteService())
        .build()
        .startAwait();

    QuoteService service = node.call().api(QuoteService.class);

    CountDownLatch latch = new CountDownLatch(3);
    Flux<String> obs = service.quotes();

    Disposable sub = obs.subscribe(onNext -> latch.countDown());

    latch.await(4, TimeUnit.SECONDS);

    sub.dispose();
    assertTrue(latch.getCount() <= 0);
    node.shutdown();
  }

  @Test
  public void test_remote_quotes_service() throws InterruptedException {
    Microservices gateway = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .build()
        .startAwait();

    Microservices node = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new SimpleQuoteService())
        .build()
        .startAwait();

    QuoteService service = gateway.call().api(QuoteService.class);
    CountDownLatch latch1 = new CountDownLatch(3);
    CountDownLatch latch2 = new CountDownLatch(3);

    Disposable sub1 = service.quotes()
        .subscribe(onNext -> {
          System.out.println("test_remote_quotes_service-2: " + onNext);
          latch1.countDown();
        });

    Disposable sub2 = service.quotes()
        .subscribe(onNext -> {
          System.out.println("test_remote_quotes_service-10: " + onNext);
          latch2.countDown();
        });

    latch1.await(4, TimeUnit.SECONDS);
    latch2.await(4, TimeUnit.SECONDS);
    sub1.dispose();
    sub2.dispose();
    assertTrue(latch1.getCount() == 0);
    assertTrue(latch2.getCount() == 0);
    gateway.shutdown();
    node.shutdown();
  }

  @Test
  public void test_quotes_batch() throws InterruptedException {
    int streamBound = 1000;

    Microservices gateway = Microservices.builder()
        .discoveryPort(port.incrementAndGet()).build().startAwait();
    Microservices node = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new SimpleQuoteService())
        .metrics(registry)
        .build()
        .startAwait();

    QuoteService service = gateway.call().api(QuoteService.class);
    CountDownLatch latch1 = new CountDownLatch(streamBound);

    Disposable sub1 = service.snapshot(streamBound)
        .subscribe(onNext -> latch1.countDown());

    latch1.await(15, TimeUnit.SECONDS);
    System.out.println("Curr value received: " + latch1.getCount());
    assertTrue(latch1.getCount() == 0);
    sub1.dispose();
    node.shutdown();
    gateway.shutdown();
  }

  @Test
  public void test_call_quotes_snapshot() throws InterruptedException {
    int batchSize = 1000;
    Microservices gateway = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .build()
        .startAwait();

    Microservices node = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new SimpleQuoteService())
        .build()
        .startAwait();

    Call service = gateway.call();

    CountDownLatch latch1 = new CountDownLatch(batchSize);
    Disposable sub1 = Flux.from(service.requestMany(Messages.builder()
        .request(QuoteService.NAME, "snapshot")
        .data(batchSize)
        .build()))
        .subscribe(onNext -> latch1.countDown());


    latch1.await(10, TimeUnit.SECONDS);
    assertTrue(latch1.getCount() == 0);
    sub1.dispose();
    gateway.shutdown();
    node.shutdown();
  }

  @Test
  public void test_just_once() {
    Microservices gateway = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .build()
        .startAwait();

    Microservices node = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new SimpleQuoteService())
        .build()
        .startAwait();

    QuoteService service = gateway.call().api(QuoteService.class);

    assertEquals("1", service.justOne().block(Duration.ofSeconds(2)));

    gateway.shutdown();
    node.shutdown();

  }

  @Test
  public void test_just_one_message() throws InterruptedException {
    int batchSize = 1;
    Microservices gateway = Microservices.builder().build().startAwait();

    Microservices node = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new SimpleQuoteService())
        .build()
        .startAwait();

    Call service = gateway.call();

    final CountDownLatch latch1 = new CountDownLatch(batchSize);
    ServiceMessage justOne = Messages.builder().request(QuoteService.NAME, "justOne").build();

    Flux.from(service.requestOne(justOne)).subscribe(onNext -> latch1.countDown());

    latch1.await(2, TimeUnit.SECONDS);
    assertTrue(latch1.getCount() == 0);
    gateway.shutdown();
    node.shutdown();
  }

  @Test
  public void test_scheduled_messages() throws InterruptedException {
    int batchSize = 1;
    Microservices gateway = Microservices.builder().build().startAwait();

    Microservices node = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new SimpleQuoteService())
        .build()
        .startAwait();

    Call service = gateway.call();

    final CountDownLatch latch1 = new CountDownLatch(batchSize);
    AtomicReference<Disposable> sub1 = new AtomicReference<>(null);
    ServiceMessage scheduled = Messages.builder().request(QuoteService.NAME, "scheduled")
        .data(1000).build();

    sub1.set(Flux.from(service.requestMany(scheduled)).subscribe(onNext -> {
      sub1.get().isDisposed();
      latch1.countDown();

    }));

    latch1.await(2, TimeUnit.SECONDS);
    assertTrue(latch1.getCount() == 0);
    node.shutdown();
    gateway.shutdown();
  }

  @Test
  public void test_unknown_method() throws InterruptedException {

    Microservices gateway = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .build()
        .startAwait();
    Microservices node = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new SimpleQuoteService())
        .build()
        .startAwait();

    Call service = gateway.call();

    final CountDownLatch latch1 = new CountDownLatch(1);

    ServiceMessage scheduled = Messages.builder().request(QuoteService.NAME, "unknonwn").build();
    try {
      service.requestMany(scheduled).blockFirst(Duration.ofSeconds(3));
    } catch (Exception ex) {
      if (ex.getMessage().contains("No reachable member with such service")) {
        latch1.countDown();
      }
    }

    latch1.await(3, TimeUnit.SECONDS);
    assertTrue(latch1.getCount() == 0);
    node.shutdown();
    gateway.shutdown();

  }

  @Test
  public void test_remote_node_died() throws InterruptedException {
    int batchSize = 1;
    Microservices gateway = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .build()
        .startAwait();

    Microservices node = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new SimpleQuoteService())
        .build()
        .startAwait();

    Call service = gateway.call();

    final CountDownLatch latch1 = new CountDownLatch(batchSize);
    AtomicReference<Disposable> sub1 = new AtomicReference<>(null);
    ServiceMessage justOne = Messages.builder().request(QuoteService.NAME, "justOne").build();

    sub1.set(Flux.from(service.requestMany(justOne)).subscribe(System.out::println));

    gateway.cluster().listenMembership()
        .filter(MembershipEvent::isRemoved)
        .subscribe(onNext -> latch1.countDown());

    node.cluster().shutdown();

    latch1.await(20, TimeUnit.SECONDS);
    Thread.sleep(100);
    assertTrue(latch1.getCount() == 0);
    assertTrue(sub1.get().isDisposed());
    gateway.shutdown();
  }
}
