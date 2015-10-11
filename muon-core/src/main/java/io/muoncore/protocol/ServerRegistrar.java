package io.muoncore.protocol;

public interface ServerRegistrar {
    void registerServerProtocol(String protocolName, ServerProtocol serverProtocol);
}
