package org.muoncore.extension.eureka;

import org.muoncore.Discovery;
import org.muoncore.ServiceDescriptor;

import java.net.URI;
import java.util.List;

public class EurekaDiscovery implements Discovery {

    @Override
    public ServiceDescriptor getService(URI uri) {
        return null;
    }

    @Override
    public List<ServiceDescriptor> getKnownServices() {
        return null;
    }

    @Override
    public void advertiseLocalService(ServiceDescriptor descriptor) {

    }

    @Override
    public void onReady(Runnable onReady) {

    }
}
