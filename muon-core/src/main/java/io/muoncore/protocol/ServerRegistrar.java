package io.muoncore.protocol;

import io.muoncore.descriptors.ProtocolDescriptor;

import java.util.List;

public interface ServerRegistrar {
    List<ProtocolDescriptor> getProtocolDescriptors();
    void registerServerProtocol(ServerProtocolStack serverProtocolStack);
}
