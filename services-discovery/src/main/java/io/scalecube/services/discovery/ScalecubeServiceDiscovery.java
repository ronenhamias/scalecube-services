package io.scalecube.services.discovery;

import static io.scalecube.services.discovery.api.ServiceDiscovery.SERVICE_METADATA;

import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.ClusterConfig;
import io.scalecube.cluster.Member;
import io.scalecube.services.ServiceEndpoint;
import io.scalecube.services.discovery.api.ServiceDiscovery;
import io.scalecube.services.registry.api.ServiceRegistry;
import io.scalecube.transport.Address;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class ScalecubeServiceDiscovery implements ServiceDiscovery {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

  private static final ObjectMapper objectMapper = newObjectMapper();

  private ServiceRegistry serviceRegistry;

  private Cluster cluster;

  private enum DiscoveryType {
    ADDED, REMOVED, DISCOVERED;
  }

  @Override
  public Mono<ServiceDiscovery> start(ServiceRegistry serviceRegistry, Object config) {
    this.serviceRegistry = serviceRegistry;

    if (config instanceof ClusterConfig.Builder) {
      ClusterConfig.Builder clusterConfig = (ClusterConfig.Builder) config;

      clusterConfig.addMetadata(this.serviceRegistry.listServiceEndpoints().stream()
          .collect(Collectors.toMap(ScalecubeServiceDiscovery::encodeMetadata, service -> SERVICE_METADATA)));
      CompletableFuture<Cluster> promise = Cluster.join(clusterConfig.build())
          .whenComplete((success,error)->{
            if(error==null) {
              this.cluster = success;
              this.init(this.cluster);
            }
          });
      
      return Mono.fromFuture(promise).map(mapper->this);
    } else {
      return Mono.empty();
    }
  }

  public void init(Cluster cluster) {
    loadClusterServices(cluster);
    listenCluster(cluster);
  }

  private void listenCluster(Cluster cluster) {
    cluster.listenMembership().subscribe(event -> {
      if (event.isAdded()) {
        loadMemberServices(DiscoveryType.ADDED, event.member());
      } else if (event.isRemoved()) {
        loadMemberServices(DiscoveryType.REMOVED, event.member());
      }
    });
  }

  private void loadClusterServices(Cluster cluster) {
    cluster.otherMembers().forEach(member -> {
      loadMemberServices(DiscoveryType.DISCOVERED, member);
    });
  }

  private void loadMemberServices(DiscoveryType type, Member member) {
    member.metadata().entrySet().stream()
        .filter(entry -> SERVICE_METADATA.equals(entry.getValue()))
        .forEach(entry -> {
          ServiceEndpoint serviceEndpoint = decodeMetadata(entry.getKey());
          if (serviceEndpoint == null) {
            return;
          }

          LOGGER.debug("Member: {} is {} : {}", member, type, serviceEndpoint);
          if ((type.equals(DiscoveryType.ADDED) || type.equals(DiscoveryType.DISCOVERED))
              && (this.serviceRegistry.registerService(serviceEndpoint))) {

            LOGGER.info("Service Reference was ADDED since new Member has joined the cluster {} : {}",
                member, serviceEndpoint);

          } else if (type.equals(DiscoveryType.REMOVED)
              && (this.serviceRegistry.unregisterService(serviceEndpoint.id()) != null)) {

            LOGGER.info("Service Reference was REMOVED since Member have left the cluster {} : {}",
                member, serviceEndpoint);
          }
        });
  }

  private static ObjectMapper newObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    return objectMapper;
  }

  public static ServiceEndpoint decodeMetadata(String metadata) {
    try {
      return objectMapper.readValue(metadata, ServiceEndpoint.class);
    } catch (IOException e) {
      LOGGER.error("Can read metadata: " + e, e);
      return null;
    }
  }

  public static String encodeMetadata(ServiceEndpoint serviceEndpoint) {
    try {
      return objectMapper.writeValueAsString(serviceEndpoint);
    } catch (IOException e) {
      LOGGER.error("Can write metadata: " + e, e);
      throw Exceptions.propagate(e);
    }
  }


  @Override
  public Mono<Void> shutdown() {
    return Mono.fromFuture(cluster.shutdown());
  }

  @Override
  public Address address() {
    return cluster.address();
  }
}
