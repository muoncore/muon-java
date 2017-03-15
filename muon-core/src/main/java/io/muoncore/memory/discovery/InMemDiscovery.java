package io.muoncore.memory.discovery;

import io.muoncore.Discovery;
import io.muoncore.InstanceDescriptor;
import io.muoncore.ServiceDescriptor;
import io.muoncore.transport.ServiceCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Designed to work in tandem with InMemTransport(s).
 */
public class InMemDiscovery implements Discovery {

    private ServiceCache cache = new ServiceCache(false);

    @Override
    public List<ServiceDescriptor> getKnownServices() {
      return cache.getServices();
    }

    @Override
    public void advertiseLocalService(InstanceDescriptor descriptor) {
      cache.addService(descriptor);
    }

    @Override
    public void onReady(DiscoveryOnReady onReady) {
        new Thread(() -> {
            try {
                onReady.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void shutdown() {

    }
}
