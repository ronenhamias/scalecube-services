package io.scalecube.services;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.cluster.membership.MembershipEvent;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.streaming.QuoteService;
import io.scalecube.services.streaming.SimpleQuoteService;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import reactor.core.Disposable;

public class ServiceTransportTest {

  private static AtomicInteger port = new AtomicInteger(6000);

  public static final ServiceMessage JUST_NEVER =
      ServiceMessage.builder().qualifier(QuoteService.NAME, "justNever").build();
  public static final ServiceMessage JUST_MANY_NEVER =
      ServiceMessage.builder().qualifier(QuoteService.NAME, "justManyNever").build();

  @Test
  public void test_remote_node_died_mono() throws Exception {
    int batchSize = 1;
    Microservices gateway = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .startAwait();

    Microservices node = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new SimpleQuoteService())
        .startAwait();

    ServiceCall.Call service = gateway.call();

    final CountDownLatch latch1 = new CountDownLatch(batchSize);
    AtomicReference<Disposable> sub1 = new AtomicReference<>(null);

    sub1.set(service.create().requestOne(JUST_NEVER)
        .subscribe(System.out::println, System.err::println));

    gateway.cluster().listenMembership()
        .filter(MembershipEvent::isRemoved)
        .subscribe(onNext -> latch1.countDown());

    // service node goes down
    TimeUnit.SECONDS.sleep(3);
    node.shutdown().block(Duration.ofSeconds(3));

    latch1.await(20, TimeUnit.SECONDS);
    TimeUnit.MILLISECONDS.sleep(100);

    assertTrue(latch1.getCount() == 0);
    assertTrue(sub1.get().isDisposed());
    gateway.shutdown();
  }

  @Test
  public void test_remote_node_died_flux() throws Exception {
    int batchSize = 1;
    Microservices gateway = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .startAwait();

    Microservices node = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new SimpleQuoteService())
        .startAwait();

    ServiceCall.Call service = gateway.call();

    final CountDownLatch latch1 = new CountDownLatch(batchSize);
    AtomicReference<Disposable> sub1 = new AtomicReference<>(null);

    sub1.set(service.create().requestMany(JUST_MANY_NEVER)
        .subscribe(System.out::println, System.err::println));

    gateway.cluster().listenMembership()
        .filter(MembershipEvent::isRemoved)
        .subscribe(onNext -> latch1.countDown());

    // service node goes down
    TimeUnit.SECONDS.sleep(3);
    node.shutdown().block(Duration.ofSeconds(3));

    latch1.await(20, TimeUnit.SECONDS);
    TimeUnit.MILLISECONDS.sleep(100);

    assertTrue(latch1.getCount() == 0);
    assertTrue(sub1.get().isDisposed());
    gateway.shutdown();
  }

}
