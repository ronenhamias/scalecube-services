package io.scalecube.services;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.cluster.membership.MembershipEvent;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.streaming.QuoteService;
import io.scalecube.services.streaming.SimpleQuoteService;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import reactor.core.Disposable;

public class ServiceTransportTest {

  private static AtomicInteger port = new AtomicInteger(6000);

  @Test
  public void test_remote_node_died() throws InterruptedException {
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
    ServiceMessage justNever = ServiceMessage.builder().qualifier(QuoteService.NAME, "justNever").build();

    sub1.set(service.create().requestOne(justNever)
        .subscribe(System.out::println, System.err::println));

    gateway.cluster().listenMembership()
        .filter(MembershipEvent::isRemoved)
        .subscribe(onNext -> latch1.countDown());

    // service node goes down
    node.shutdown();

    latch1.await(20, TimeUnit.SECONDS);
    Thread.sleep(100);
    assertTrue(latch1.getCount() == 0);
    assertTrue(sub1.get().isDisposed());
    gateway.shutdown();
  }

}
