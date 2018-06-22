package io.scalecube.services;

import static io.scalecube.services.TestRequests.GREETING_REQUEST_REQ;
import static io.scalecube.services.TestRequests.GREETING_REQUEST_REQ2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.services.ServiceCall.Call;
import io.scalecube.services.a.b.testing.CanaryService;
import io.scalecube.services.a.b.testing.CanaryTestingRouter;
import io.scalecube.services.a.b.testing.GreetingServiceImplA;
import io.scalecube.services.a.b.testing.GreetingServiceImplB;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.example.DummyRouter;
import io.scalecube.services.example.GreetingServiceImpl;
import io.scalecube.services.example.api.GreetingRequest;
import io.scalecube.services.example.api.GreetingResponse;
import io.scalecube.services.routing.RandomServiceRouter;
import io.scalecube.services.routing.Routers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import reactor.core.publisher.Mono;

public class RoutersTest extends BaseTest {
  public static final int TIMEOUT = 3;
  private Duration timeout = Duration.ofSeconds(TIMEOUT);

  private Microservices gateway;

  @BeforeEach
  public void setup() {
    this.gateway = Microservices.builder().startAwait();
  }

  @AfterEach
  public void tearDown() {
    gateway.shutdown().block();
  }

  @Test
  public void test_router_factory() {
    assertNotNull(Routers.getRouter(RandomServiceRouter.class));

    // dummy router will always throw exception thus cannot be created.
    assertThrows(IllegalArgumentException.class, () -> Routers.getRouter(DummyRouter.class));
  }

  @Test
  public void test_round_robin_selection_logic() {
    // Create microservices instance cluster.
    Microservices provider1 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .services(new GreetingServiceImpl(1))
        .startAwait();

    // Create microservices instance cluster.
    Microservices provider2 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .services(new GreetingServiceImpl(2))
        .startAwait();

    ServiceCall service = gateway.call().create();

    // call the service.
    GreetingResponse result1 =
        Mono.from(service.requestOne(GREETING_REQUEST_REQ, GreetingResponse.class)).timeout(timeout).block()
            .data();
    GreetingResponse result2 =
        Mono.from(service.requestOne(GREETING_REQUEST_REQ, GreetingResponse.class)).timeout(timeout).block()
            .data();

    assertTrue(!result1.sender().equals(result2.sender()));
    provider2.shutdown().block();
    provider1.shutdown().block();
  }

  @Test
  public void test_tag_selection_logic() {

    // Create microservices instance cluster.
    Microservices provider1 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .service(new GreetingServiceImpl(1)).tag("SENDER", "1").register()
        .startAwait();

    // Create microservices instance cluster.
    Microservices provider2 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .service(new GreetingServiceImpl(2)).tag("SENDER", "2").register()
        .startAwait();

    Call service = gateway.call().router((reg, msg) -> reg.listServiceReferences().stream().filter(ref -> "2".equals(
        ref.tags().get("SENDER"))).findFirst());

    // call the service.
    for (int i = 0; i < 1e3; i++) {
      GreetingResponse result =
          Mono.from(service.create().requestOne(GREETING_REQUEST_REQ, GreetingResponse.class)).timeout(timeout).block()
              .data();
      assertEquals("2", result.sender());
    }
    provider2.shutdown().block();
    provider1.shutdown().block();
  }

  @Test
  public void test_tag_request_selection_logic() {
    // Create microservices instance cluster.
    Microservices provider1 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .service(new GreetingServiceImpl(1)).tag("ONLYFOR", "joe").register()
        .startAwait();

    // Create microservices instance cluster.
    Microservices provider2 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .service(new GreetingServiceImpl(2)).tag("ONLYFOR", "fransin").register()
        .startAwait();

    ServiceCall service = gateway.call().router(
        (reg, msg) -> reg.listServiceReferences().stream().filter(ref -> ((GreetingRequest) msg.data()).getName()
            .equals(ref.tags().get("ONLYFOR"))).findFirst())
        .create();

    // call the service.
    for (int i = 0; i < 1e2; i++) {
      GreetingResponse resultForFransin =
          service.requestOne(GREETING_REQUEST_REQ2, GreetingResponse.class).block(timeout).data();
      GreetingResponse resultForJoe =
          service.requestOne(GREETING_REQUEST_REQ, GreetingResponse.class).block(timeout).data();
      assertEquals("1", resultForJoe.sender());
      assertEquals("2", resultForFransin.sender());
    }
    provider2.shutdown().block();
    provider1.shutdown().block();
  }

  @Test
  public void test_service_tags() throws Exception {
    Microservices services1 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .service(new GreetingServiceImplA()).tag("Weight", "0.3").register()
        .startAwait();

    Microservices services2 = Microservices.builder()
        .seeds(gateway.cluster().address())
        .service(new GreetingServiceImplB()).tag("Weight", "0.7").register()
        .startAwait();

    System.out.println(gateway.cluster().members());

    TimeUnit.SECONDS.sleep(3);
    ServiceCall service = gateway.call().router(CanaryTestingRouter.class).create();

    ServiceMessage req = ServiceMessage.builder()
        .qualifier(Reflect.serviceName(CanaryService.class), "greeting")
        .data(new GreetingRequest("joe"))
        .build();

    AtomicInteger serviceBCount = new AtomicInteger(0);

    int n = (int) 1e2;
    for (int i = 0; i < n; i++) {
      ServiceMessage message = service.requestOne(req, GreetingResponse.class).block(timeout);
      if (message.data().toString().contains("SERVICE_B_TALKING")) {
        serviceBCount.incrementAndGet();
      }
    }

    System.out.println("Service B was called: " + serviceBCount.get() + " times.");

    assertEquals(0.6d, serviceBCount.doubleValue() / n, 0.25d);

    services1.shutdown().block();
    services2.shutdown().block();
  }

}
