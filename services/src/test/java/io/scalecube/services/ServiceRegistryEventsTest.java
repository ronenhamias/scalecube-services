package io.scalecube.services;

import static io.scalecube.services.registry.api.RegistrationEvent.Type.ADDED;
import static io.scalecube.services.registry.api.RegistrationEvent.Type.REMOVED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.scalecube.services.registry.api.RegistrationEvent;
import io.scalecube.services.sut.GreetingServiceImpl;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ServiceRegistryEventsTest {

  @Test
  public void test_added_removed_registration_events() throws InterruptedException {

    List<RegistrationEvent> events = new ArrayList<>();
    
    Microservices seed = Microservices.builder()
        .startAwait();

    seed.serviceRegistry().listen().subscribe(events::add);

    Microservices ms1 = Microservices.builder().seeds(seed.cluster().address())
        .services(new GreetingServiceImpl())
        .startAwait();

    Microservices ms2 = Microservices.builder().seeds(seed.cluster().address())
        .services(new GreetingServiceImpl())
        .startAwait();

    ms1.shutdown().block();
    ms2.shutdown().block();

    Thread.sleep(100);
    assertEquals(4, events.size());
    assertEquals(ADDED, events.get(0).type());
    assertEquals(ADDED, events.get(1).type());
    assertEquals(REMOVED, events.get(2).type());
    assertEquals(REMOVED, events.get(3).type());

  }
}