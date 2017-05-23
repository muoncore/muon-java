package io.muoncore.memory.discovery;

import io.muoncore.Discovery;
import io.muoncore.InstanceDescriptor;
import io.muoncore.ServiceDescriptor;
import io.muoncore.transport.ServiceCache;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Designed to work in tandem with InMemTransport(s).
 */
public class InMemDiscovery implements Discovery {

    private ServiceCache cache = new ServiceCache(false);

    @Override
    public List<String> getServiceNames() {
      return cache.getServices().stream().map(ServiceDescriptor::getIdentifier).collect(Collectors.toList());
    }

    @Override
    public Optional<ServiceDescriptor> getServiceNamed(String name) {
      return cache.getServices().stream().filter(serviceDescriptor -> serviceDescriptor.getIdentifier().equals(name)).findAny();
    }

    @Override
    public Optional<ServiceDescriptor> getServiceWithTags(String... tags) {
      return cache.getServices().stream().filter(serviceDescriptor -> serviceDescriptor.getTags().containsAll(Arrays.asList(tags))).findAny();
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
