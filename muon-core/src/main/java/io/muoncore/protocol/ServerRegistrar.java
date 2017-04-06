package io.muoncore.protocol;

import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.descriptors.SchemasDescriptor;

import java.util.List;

public interface ServerRegistrar {

  List<ProtocolDescriptor> getProtocolDescriptors();

  void registerServerProtocol(ServerProtocolStack serverProtocolStack);

  SchemasDescriptor getSchemasDescriptor(String protocol, String resource);
}
