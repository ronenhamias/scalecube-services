package io.scalecube.services.routing;

import io.scalecube.services.ServiceReference;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.registry.api.ServiceRegistry;

import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface Router {

  /**
   * Returns suitable service references for a given request message.
   *
   * @param serviceRegistry service registry
   * @param request service message
   * @return service instance
   */
  Optional<ServiceReference> route(ServiceRegistry serviceRegistry, ServiceMessage request);

  /**
   * Returns all suitable service references for particular service message.
   *
   * @param serviceRegistry service registry
   * @param request service message
   * @return list of suitable routes
   */
  default List<ServiceReference> routes(ServiceRegistry serviceRegistry, ServiceMessage request) {
    return serviceRegistry.lookupService(request.qualifier());
  }

}
