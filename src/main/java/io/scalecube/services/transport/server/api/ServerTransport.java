package io.scalecube.services.transport.server.api;

import io.scalecube.transport.Address;

public interface ServerTransport {

  static ServerTransport newServer() {
    // TODO Auto-generated method stub
    return null;
  }

  Address bindAwait();
  
}
