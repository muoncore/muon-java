package io.muoncore;

import io.muoncore.protocol.ServerRegistrar;

public interface ServerRegistrarSource {
    ServerRegistrar getProtocolStacks();
}
