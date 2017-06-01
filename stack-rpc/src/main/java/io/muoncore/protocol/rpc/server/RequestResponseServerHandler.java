package io.muoncore.protocol.rpc.server;

import io.muoncore.descriptors.SchemaDescriptor;

import java.util.Map;

public interface RequestResponseServerHandler {
    HandlerPredicate getPredicate();
    void handle(RequestWrapper request);
    Map<String, SchemaDescriptor> getDescriptors();
}
