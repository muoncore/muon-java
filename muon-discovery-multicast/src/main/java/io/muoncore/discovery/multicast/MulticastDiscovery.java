package io.muoncore.discovery.multicast;

import io.muoncore.Discovery;
import io.muoncore.InstanceDescriptor;
import io.muoncore.ServiceDescriptor;
import io.muoncore.exception.MuonException;
import io.muoncore.transport.ServiceCache;

import java.io.IOException;
import java.util.List;

public class MulticastDiscovery implements Discovery {

    public static int PORT = 9898;
    public static String MULTICAST_ADDRESS = "224.1.7.8";

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
    public void advertiseLocalService(InstanceDescriptor descriptor) {
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
      onReady.call();
    }

    @Override
    public void shutdown() {
        server.shutdown();
        client.shutdown();
    }
}
