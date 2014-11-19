package org.muoncore.extension.http;

import org.muoncore.ServiceDescriptor;

import java.util.List;

public class EtcdServiceDiscovery implements HttpTransportServiceDiscovery {
    @Override
    public void register() {

    }

    @Override
    public void unregister() {

    }

    @Override
    public List<ServiceDescriptor> discover() {
        return null;
    }
}
