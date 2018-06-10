package io.scalecube.services.discovery;

import io.scalecube.cluster.membership.IdGenerator;
import io.scalecube.services.CommunicationMode;
import io.scalecube.services.Microservices;
import io.scalecube.services.Reflect;
import io.scalecube.services.ServiceEndpoint;
import io.scalecube.services.ServiceMethodDefinition;
import io.scalecube.services.ServiceRegistration;
import io.scalecube.services.annotations.ContentType;
import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceScanner {

  public static ServiceEndpoint scan(List<Microservices.ServiceInfo> serviceInstances, String host, int port,
      Map<String, String> endpointTags) {
    String endpointId = IdGenerator.generateId();
    List<ServiceRegistration> serviceRegistrations = serviceInstances.stream()
        .flatMap(inst -> Arrays.stream(inst.service().getClass().getInterfaces())
            .map(serviceInterface -> new InterfaceInfo(serviceInterface, inst.tags())))
        .filter(iAndTags -> iAndTags.serviceInterface.isAnnotationPresent(Service.class))
        .map(iAndTags -> {
          Class<?> serviceInterface = iAndTags.serviceInterface;
          Map<String, String> serviceTags = iAndTags.tags;
          String namespace = Reflect.serviceName(serviceInterface);
          ContentType ctAnnotation = serviceInterface.getAnnotation(ContentType.class);
          String serviceContentType = ctAnnotation != null ? ctAnnotation.value() : ContentType.DEFAULT;
          List<ServiceMethodDefinition> actions = Arrays.stream(serviceInterface.getMethods())
              .filter(m -> m.isAnnotationPresent(ServiceMethod.class))
              .map(m -> {
                String action = Reflect.methodName(m);
                String contentType = ContentType.DEFAULT;
                // Map<String, String> methodTags = methodTags(m);
                CommunicationMode communicationMode = Reflect.communicationMode(m);
                return new ServiceMethodDefinition(action, contentType, communicationMode);
              }).collect(Collectors.toList());
          return new ServiceRegistration(namespace,
              serviceContentType,
              serviceTags,
              actions);
        }).collect(Collectors.toList());
    return new ServiceEndpoint(endpointId, host, port, endpointTags, serviceRegistrations);
  }

  private static class InterfaceInfo {
    private final Class<?> serviceInterface;
    private final Map<String, String> tags;

    private InterfaceInfo(Class<?> serviceInterface, Map<String, String> tags) {
      this.serviceInterface = serviceInterface;
      this.tags = tags;
    }
  }

  // private static String merge(String lowPriority, String highPriority) {
  // return highPriority == null ? lowPriority : highPriority;
  // }

  // private static Map<String, String> merge(Map<String, String> lowPriority, Map<String, String> highPriority) {
  // Map<String, String> result = new HashMap<>();
  // result.putAll(lowPriority);
  // result.putAll(highPriority);
  // return result;
  // }

  // private static Map<String, String> methodTags(Method method) {
  // // TODO: tags are not yet implemented on API level
  // return new HashMap<>();
  // }
}
