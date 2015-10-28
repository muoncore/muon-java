package io.muoncore.memory.discovery;

import io.muoncore.Discovery;
import io.muoncore.ServiceDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Designed to work in tandem with InMemTransport(s).
 */
public class InMemDiscovery implements Discovery {

    private List<ServiceDescriptor> services = new ArrayList<>();

    @Override
    public List<ServiceDescriptor> getKnownServices() {
        return Collections.unmodifiableList(services);
    }

    @Override
    public void advertiseLocalService(ServiceDescriptor descriptor) {
        services.add(descriptor);
    }

    @Override
    public void onReady(Runnable onReady) {
        new Thread(onReady).start();
    }
}
