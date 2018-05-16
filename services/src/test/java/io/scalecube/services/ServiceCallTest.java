package io.scalecube.services;

import static io.scalecube.services.TestRequests.GREETING_ERROR_REQ;
import static io.scalecube.services.TestRequests.GREETING_FAIL_REQ;
import static io.scalecube.services.TestRequests.GREETING_NO_PARAMS_REQUEST;
import static io.scalecube.services.TestRequests.GREETING_REQ;
import static io.scalecube.services.TestRequests.GREETING_REQUEST_REQ;
import static io.scalecube.services.TestRequests.GREETING_REQUEST_REQ2;
import static io.scalecube.services.TestRequests.GREETING_REQUEST_TIMEOUT_REQ;
import static io.scalecube.services.TestRequests.GREETING_VOID_REQ;
import static io.scalecube.services.TestRequests.NOT_FOUND_REQ;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.scalecube.services.ServiceCall.Call;
import io.scalecube.services.a.b.testing.CanaryService;
import io.scalecube.services.a.b.testing.CanaryTestingRouter;
import io.scalecube.services.a.b.testing.GreetingServiceImplA;
import io.scalecube.services.a.b.testing.GreetingServiceImplB;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.exceptions.ServiceException;
import io.scalecube.services.routing.RoundRobinServiceRouter;
import io.scalecube.services.routing.Router;
import io.scalecube.services.routing.Routers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.reactivestreams.Publisher;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

public class ServiceCallTest extends BaseTest {

  public static final int TIMEOUT = 3;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Duration timeout = Duration.ofSeconds(TIMEOUT);



  private static AtomicInteger port = new AtomicInteger(4000);


  @Test
  public void test_local_async_no_params() throws Exception {
    // Create microservices cluster.
    Microservices microservices = serviceProvider();

    Call serviceCall = microservices.call().router(RoundRobinServiceRouter.class);

    // call the service.
    Publisher<ServiceMessage> future =
        serviceCall.requestOne(GREETING_NO_PARAMS_REQUEST);

    ServiceMessage message = Mono.from(future).block(Duration.ofSeconds(TIMEOUT));

    assertEquals("Didn't get desired response", GREETING_NO_PARAMS_REQUEST.qualifier(), message.qualifier());

    microservices.shutdown().block();
  }

  private Microservices serviceProvider() {
    return Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .services(new GreetingServiceImpl())
        .startAwait();
  }

  @Test
  public void test_remote_async_greeting_no_params() {
    // Create microservices cluster.
    Microservices provider = serviceProvider();

    // Create microservices cluster.
    Microservices consumer = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(provider.cluster().address())
        .startAwait();

    Call serviceCall = consumer.call();

    // call the service.
    Publisher<ServiceMessage> future =
        serviceCall.requestOne(GREETING_NO_PARAMS_REQUEST, GreetingResponse.class);

    ServiceMessage message = Mono.from(future).block(timeout);

    assertThat(message.data(), instanceOf(GreetingResponse.class));
    assertTrue(((GreetingResponse) message.data()).getResult().equals("hello unknown"));

    provider.shutdown().block();
    consumer.shutdown().block();
  }

  @Test
  public void test_remote_void_greeting() throws Exception {
    // Given
    Microservices gateway = gateway();

    CountDownLatch signal = new CountDownLatch(1);
    Microservices node1 = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new GreetingServiceImpl(signal))
        .startAwait();

    // When
    AtomicReference<SignalType> success = new AtomicReference<>();
    gateway.call().oneWay(GREETING_VOID_REQ)
        .doFinally(success::set)
        .timeout(Duration.ofSeconds(TIMEOUT))
        .block();

    // Then:
    signal.await(2, TimeUnit.SECONDS);
    assertTrue(signal.getCount() == 0);
    assertNotNull(success.get());
    assertEquals(SignalType.ON_COMPLETE, success.get());

    gateway.shutdown().block();
    node1.shutdown().block();
  }

  @Test
  public void test_remote_fail_greeting() throws InterruptedException {
    thrown.expect(ServiceException.class);
    thrown.expectMessage("GreetingRequest{name='joe'}");

    // Given
    Microservices gateway = gateway();

    Microservices node1 = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new GreetingServiceImpl())
        .startAwait();

    // When
    Mono.from(gateway.call().requestOne(GREETING_FAIL_REQ)).block(timeout);

    gateway.shutdown().block();
    node1.shutdown().block();
  }

  @Test
  public void test_remote_exception_void() throws Exception {
    thrown.expect(ServiceException.class);
    thrown.expectMessage("GreetingRequest{name='joe'}");

    // Given
    Microservices gateway = gateway();

    Microservices node1 = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new GreetingServiceImpl())
        .startAwait();

    // When
    Mono.from(gateway.call().requestOne(GREETING_ERROR_REQ)).block(timeout);

    gateway.shutdown().block();
    node1.shutdown().block();
  }

  @Test
  public void test_local_void_greeting() throws Exception {
    // GIVEN
    Microservices node = serviceProvider();


    // WHEN
    AtomicReference<SignalType> success = new AtomicReference<>();
    node.call().oneWay(GREETING_VOID_REQ).doFinally(success::set).block(Duration.ofSeconds(TIMEOUT));

    // Then:
    assertNotNull(success.get());
    assertEquals(SignalType.ON_COMPLETE, success.get());

    TimeUnit.SECONDS.sleep(2);
    node.shutdown().block();
  }


  @Test
  public void test_local_fail_greeting() throws Exception {
    thrown.expect(ServiceException.class);
    thrown.expectMessage("GreetingRequest{name='joe'}");

    // Create microservices instance.
    Microservices node = serviceProvider();

    // call the service.
    Mono.from(node.call().requestOne(GREETING_FAIL_REQ)).block(timeout);

    node.shutdown().block();
  }

  @Test
  public void test_local_exception_greeting() throws Exception {
    thrown.expect(ServiceException.class);
    thrown.expectMessage("GreetingRequest{name='joe'}");

    // Create microservices instance.
    Microservices node = serviceProvider();

    // call the service.
    Mono.from(node.call().requestOne(GREETING_ERROR_REQ)).block(timeout);

    node.shutdown().block();
  }

  @Test
  public void test_remote_async_greeting_return_string() throws Exception {
    // Create microservices cluster.
    Microservices provider = serviceProvider();

    // Create microservices cluster.
    Microservices consumer = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(provider.cluster().address())
        .startAwait();

    Publisher<ServiceMessage> resultFuture = consumer.call().requestOne(GREETING_REQ);

    // Then
    ServiceMessage result = Mono.from(resultFuture).block(Duration.ofSeconds(TIMEOUT));
    assertNotNull(result);
    assertEquals(GREETING_REQ.qualifier(), result.qualifier());
    assertEquals(" hello to: joe", result.data());

    provider.shutdown().block();
    consumer.shutdown().block();
  }

  @Test
  public void test_local_async_greeting_return_GreetingResponse() {
    // Given
    Microservices microservices = serviceProvider();

    // When
    Publisher<ServiceMessage> resultFuture =
        microservices.call().requestOne(GREETING_REQUEST_REQ);

    // Then
    ServiceMessage result = Mono.from(resultFuture).block(Duration.ofSeconds(TIMEOUT));
    assertNotNull(result);
    assertEquals(GREETING_REQUEST_REQ.qualifier(), result.qualifier());
    assertEquals(" hello to: joe", ((GreetingResponse) result.data()).getResult());

    microservices.shutdown().block();
  }

  @Test
  public void test_remote_async_greeting_return_GreetingResponse()
      throws InterruptedException, ExecutionException, TimeoutException {
    // Given
    Microservices provider = serviceProvider();
    Microservices consumer = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(provider.cluster().address())
        .startAwait();

    // When
    Publisher<ServiceMessage> result =
        consumer.call().requestOne(GREETING_REQUEST_REQ, GreetingResponse.class);

    // Then
    GreetingResponse greeting = Mono.from(result).block(Duration.ofSeconds(TIMEOUT)).data();
    assertEquals(" hello to: joe", greeting.getResult());

    provider.shutdown().block();
    consumer.shutdown().block();
  }

  @Test
  public void test_local_greeting_request_timeout_expires() {
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Timeout on blocking read");

    // Given:
    Microservices node = serviceProvider();
    Call service = node.call();

    // call the service.
    Publisher<ServiceMessage> future =
        service.requestOne(GREETING_REQUEST_TIMEOUT_REQ);

    try {
      Mono.from(future).block(Duration.ofSeconds(1));
    } finally {
      node.shutdown().block();
    }

  }

  @Test
  public void test_remote_greeting_request_timeout_expires() {
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Timeout on blocking read");

    // Create microservices cluster.
    Microservices provider = serviceProvider();

    // Create microservices cluster.
    Microservices consumer = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(provider.cluster().address())
        .startAwait();

    Call service = consumer.call();

    // call the service.
    Publisher<ServiceMessage> future =
        service.requestOne(GREETING_REQUEST_TIMEOUT_REQ);
    try {
      Mono.from(future).block(Duration.ofSeconds(1));
    } finally {
      provider.shutdown().block();
      consumer.shutdown().block();
    }
  }

  // Since here and below tests were not reviewed [sergeyr]

  @Test
  public void test_local_async_greeting_return_Message() throws InterruptedException, ExecutionException {
    // Given:
    Microservices microservices = serviceProvider();

    Call service = microservices.call();

    // call the service.
    Publisher<ServiceMessage> future =
        service.requestOne(GREETING_REQUEST_REQ);


    CountDownLatch timeLatch = new CountDownLatch(1);
    Mono.from(future).doOnNext(result -> {

      assertTrue(result.data().equals(" hello to: joe"));
      // print the greeting.
      System.out.println("9. local_async_greeting_return_Message :" + result.data());

      timeLatch.countDown();
    });
    TimeUnit.SECONDS.sleep(1);
    microservices.shutdown().block();
  }

  @Test
  public void test_remote_async_greeting_return_Message() throws Exception {
    // Create microservices cluster.
    Microservices provider = serviceProvider();

    // Create microservices cluster.
    Microservices consumer = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(provider.cluster().address())
        .startAwait();

    Call service = consumer.call();

    // call the service.
    Publisher<ServiceMessage> future =
        service.requestOne(GREETING_REQUEST_REQ);

    Mono.from(future).doOnNext(result -> {
      // print the greeting.
      System.out.println("10. remote_async_greeting_return_Message :" + result.data());
      // print the greeting.
      assertThat(result.data(), instanceOf(GreetingResponse.class));
      assertTrue(((GreetingResponse) result.data()).getResult().equals(" hello to: joe"));
    });

    consumer.shutdown().block();
    provider.shutdown().block();
  }

  @Test
  public void test_round_robin_selection_logic() throws Exception {
    Microservices gateway = gateway();

    // Create microservices instance cluster.
    Microservices provider1 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .discoveryPort(port.incrementAndGet())
        .services(new GreetingServiceImpl(1))
        .startAwait();

    // Create microservices instance cluster.
    Microservices provider2 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .discoveryPort(port.incrementAndGet())
        .services(new GreetingServiceImpl(2))
        .startAwait();

    Call service = gateway.call();

    // call the service.
    GreetingResponse result1 =
        Mono.from(service.requestOne(GREETING_REQUEST_REQ, GreetingResponse.class)).timeout(timeout).block().data();
    GreetingResponse result2 =
        Mono.from(service.requestOne(GREETING_REQUEST_REQ, GreetingResponse.class)).timeout(timeout).block().data();

    assertTrue(result1.sender() != result2.sender());
    provider2.shutdown().block();
    provider1.shutdown().block();
    gateway.shutdown().block();
  }


  @Test
  public void test_tag_selection_logic() throws Exception {
    Microservices gateway = gateway();

    // Create microservices instance cluster.
    Microservices provider1 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .discoveryPort(port.incrementAndGet())
        .service(new GreetingServiceImpl(1)).tag("SENDER", "1").register()
        .startAwait();

    // Create microservices instance cluster.
    Microservices provider2 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .discoveryPort(port.incrementAndGet())
        .service(new GreetingServiceImpl(2)).tag("SENDER", "2").register()
        .startAwait();

    Call service = gateway.call().router((reg, msg) -> 
      reg.listServiceReferences().stream().filter(ref -> "2".equals(
          ref.tags().get("SENDER"))).collect(Collectors.toList()));

    // call the service.
   for (int i = 0; i < 10 ; i++) {
     GreetingResponse result =
         Mono.from(service.requestOne(GREETING_REQUEST_REQ, GreetingResponse.class)).timeout(timeout).block().data();
     assertEquals("2", result.sender());
   }
    provider2.shutdown().block();
    provider1.shutdown().block();
    gateway.shutdown().block();
  }
  
  @Test
  public void test_tag_request_selection_logic() throws Exception {
    Microservices gateway = gateway();

    // Create microservices instance cluster.
    Microservices provider1 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .discoveryPort(port.incrementAndGet())
        .service(new GreetingServiceImpl(1)).tag("ONLYFOR", "joe").register()
        .startAwait();

    // Create microservices instance cluster.
    Microservices provider2 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .discoveryPort(port.incrementAndGet())
        .service(new GreetingServiceImpl(2)).tag("ONLYFOR", "fransin").register()
        .startAwait();

    Call service = gateway.call().router((reg, msg) -> 
    reg.listServiceReferences().stream().filter(ref -> ((GreetingRequest)msg.data()).getName()
        .equals(ref.tags().get("ONLYFOR"))).collect(Collectors.toList())
      );

    // call the service.
   for (int i = 0; i < 10 ; i++) {
     GreetingResponse resultForFransin =
         Mono.from(service.requestOne(GREETING_REQUEST_REQ2, GreetingResponse.class)).timeout(timeout).block().data();
     GreetingResponse resultForJoe =
         Mono.from(service.requestOne(GREETING_REQUEST_REQ, GreetingResponse.class)).timeout(timeout).block().data();
     assertEquals("1", resultForJoe.sender());
     assertEquals("2", resultForFransin.sender());
   }
    provider2.shutdown().block();
    provider1.shutdown().block();
    gateway.shutdown().block();
  }

  @Test
  public void test_async_greeting_return_string_service_not_found_error_case()
      throws Exception {
    Microservices gateway = gateway();

    // Create microservices instance cluster.
    Microservices provider1 = provider(gateway);

    Call service = provider1.call();

    CountDownLatch timeLatch = new CountDownLatch(1);
    try {
      // call the service.
      Publisher<ServiceMessage> future = service.requestOne(NOT_FOUND_REQ);

    } catch (Exception ex) {
      assertTrue(ex.getMessage().equals("No reachable member with such service: " + NOT_FOUND_REQ.qualifier()));
      timeLatch.countDown();
    }

    assertTrue(await(timeLatch, 1, TimeUnit.SECONDS));
    gateway.shutdown().block();
    provider1.shutdown().block();
  }

  @Test
  public void test_service_tags() throws Exception {
    Microservices gateway = gateway();

    Microservices services1 = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .service(new GreetingServiceImplA()).tag("Weight", "0.3").register()
        .startAwait();

    Microservices services2 = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .service(new GreetingServiceImplB()).tag("Weight", "0.7").register()
        .startAwait();

    System.out.println(gateway.cluster().members());

    TimeUnit.SECONDS.sleep(3);
    Call service = gateway.call().router(CanaryTestingRouter.class);

    ServiceMessage req = Messages.builder()
        .request(CanaryService.class, "greeting")
        .data(new GreetingRequest("joe"))
        .build();

    AtomicInteger count = new AtomicInteger(0);
    AtomicInteger responses = new AtomicInteger(0);
    CountDownLatch timeLatch = new CountDownLatch(1);

    int n = 100;
    for (int i = 0; i < n; i++) {
      ServiceMessage success = Mono.from(service.requestOne(req)).block();
      responses.incrementAndGet();
      if (success.data().toString().contains("SERVICE_B_TALKING")) {
        count.incrementAndGet();
        if ((responses.get() == n) && (60 < count.get() && count.get() < 85)) {
          timeLatch.countDown();
        }
      }
    }
    System.out.println("responses: " + responses.get());
    System.out.println("count: " + count.get());
    System.out.println("Service B was called: " + count.get() + " times.");

    assertTrue((responses.get() == n) && (60 < count.get() && count.get() < 85));
    services1.shutdown().block();
    services2.shutdown().block();
    gateway.shutdown().block();

  }

  @Test
  public void test_dispatcher_remote_greeting_request_completes_before_timeout() throws Exception {

    // Create microservices instance.
    Microservices gateway = gateway();

    Microservices node = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .seeds(gateway.cluster().address())
        .services(new GreetingServiceImpl())
        .startAwait();

    Publisher<ServiceMessage> result = gateway.call().requestOne(GREETING_REQUEST_REQ, GreetingResponse.class);

    GreetingResponse greetings = Mono.from(result).block(Duration.ofSeconds(TIMEOUT)).data();
    System.out.println("greeting_request_completes_before_timeout : " + greetings.getResult());
    assertTrue(greetings.getResult().equals(" hello to: joe"));

    gateway.shutdown().block();
    node.shutdown().block();
  }

  @Test
  public void test_dispatcher_local_greeting_request_completes_before_timeout() {

    Microservices gateway = Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .services(new GreetingServiceImpl())
        .startAwait();

    Call service = gateway.call();

    Publisher<ServiceMessage> result = service.requestOne(GREETING_REQUEST_REQ, GreetingResponse.class);

    GreetingResponse greetings = Mono.from(result).timeout(Duration.ofSeconds(TIMEOUT)).block().data();
    System.out.println("1. greeting_request_completes_before_timeout : " + greetings.getResult());
    assertTrue(greetings.getResult().equals(" hello to: joe"));

    gateway.shutdown().block();
  }

  private Microservices provider(Microservices gateway) {
    return Microservices.builder()
        .seeds(gateway.cluster().address())
        .discoveryPort(port.incrementAndGet())
        .startAwait();
  }

  private Microservices gateway() {
    return Microservices.builder()
        .discoveryPort(port.incrementAndGet())
        .startAwait();
  }

  private boolean await(CountDownLatch timeLatch, long timeout, TimeUnit timeUnit) throws Exception {
    return timeLatch.await(timeout, timeUnit);
  }
}
