package org.muoncore.extension.http;

import org.muoncore.ServiceDescriptor;

import java.util.List;

public interface HttpTransportServiceDiscovery {
    void register();
    void unregister();
    List<ServiceDescriptor> discover();
}
