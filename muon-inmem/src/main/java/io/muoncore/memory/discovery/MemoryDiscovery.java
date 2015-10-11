package io.muoncore.memory.discovery;

import io.muoncore.Discovery;
import io.muoncore.ServiceDescriptor;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Designed to work in tandem with a InMemTransport.
 */
public class MemoryDiscovery implements Discovery {

    @Override
    public Optional<ServiceDescriptor> getService(URI uri) {
        return null;
    }

    @Override
    public Optional<ServiceDescriptor> findService(Predicate<ServiceDescriptor> predicate) {
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
