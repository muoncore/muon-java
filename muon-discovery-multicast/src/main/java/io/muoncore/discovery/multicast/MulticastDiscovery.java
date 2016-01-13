package io.muoncore.discovery.multicast;

import io.muoncore.Discovery;
import io.muoncore.ServiceDescriptor;
import io.muoncore.exception.MuonException;
import io.muoncore.transport.ServiceCache;

import java.io.IOException;
import java.util.List;

public class MulticastDiscovery implements Discovery {

    private ServiceCache serviceCache;
    private MulticastClient client;
    private MulticastServerThread server;

    public MulticastDiscovery(ServiceCache serviceCache) {
        this.serviceCache = serviceCache;
        this.client = new MulticastClient(serviceCache);
        client.start();
    }

    @Override
    public List<ServiceDescriptor> getKnownServices() {
        return serviceCache.getServices();
    }

    @Override
    public void advertiseLocalService(ServiceDescriptor descriptor) {
        if (server != null) throw new MuonException("Discovery is already advertising service");
        try {
            this.server = new MulticastServerThread(descriptor);
            this.server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReady(DiscoveryOnReady onReady) {

    }

    @Override
    public void shutdown() {
        server.shutdown();
        client.shutdown();
    }
}
