package io.scalecube.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static reactor.core.publisher.Mono.from;

import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.exceptions.BadRequestException;
import io.scalecube.services.exceptions.ServiceUnavailableException;
import io.scalecube.services.exceptions.UnauthorizedException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;

import java.util.concurrent.atomic.AtomicInteger;

public class ErrorFlowTest {

  private static AtomicInteger port = new AtomicInteger(4000);
  private static Microservices provider;
  private static Microservices consumer;


  @BeforeAll
  public static void initNodes() {
    provider = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .services(new GreetingServiceImpl())
        .build()
        .startAwait();
    consumer = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(provider.cluster().address())
        .build()
        .startAwait();
  }

  @AfterAll
  public static void shutdownNodes() {
    consumer.shutdown().block();
    provider.shutdown().block();
  }

  @Test
  public void testCorruptedRequest() {
    Publisher<ServiceMessage> req = consumer
        .call().requestResponse(TestRequests.GREETING_CORRUPTED_PAYLOAD_REQUEST);
    assertThrows(BadRequestException.class, () -> from(req).block());
  }

  @Test
  public void testNotAuthorized() {
    Publisher<ServiceMessage> req = consumer
        .call().requestResponse(TestRequests.GREETING_UNAUTHORIZED_REQUEST);
    assertThrows(UnauthorizedException.class, () -> from(req).block());
  }

  @Test
  public void testNullRequestPayload() {
    Publisher<ServiceMessage> req = consumer
        .call().requestResponse(TestRequests.GREETING_NULL_PAYLOAD);
    assertThrows(BadRequestException.class, () -> from(req).block());
  }

  @Test
  public void testServiceUnavailable() {
    Publisher<ServiceMessage> req = consumer
        .call().requestResponse(TestRequests.NOT_FOUND_REQ);
    assertThrows(ServiceUnavailableException.class, () -> from(req).block());
  }
}
