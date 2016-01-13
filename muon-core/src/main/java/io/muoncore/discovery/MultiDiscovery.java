package io.muoncore.discovery;

import io.muoncore.Discovery;
import io.muoncore.ServiceDescriptor;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MultiDiscovery implements Discovery {

    private List<Discovery> discoveries;
    private CountDownLatch onReadyLatch;

    public MultiDiscovery(List<Discovery> discoveries) {
        this.discoveries = discoveries;
        onReadyLatch = new CountDownLatch(discoveries.size());
    }

    @Override
    public List<ServiceDescriptor> getKnownServices() {

        List<ServiceDescriptor> desc =  discoveries.stream().flatMap( it -> it.getKnownServices().stream()).distinct().collect(Collectors.toList());

        return desc;
    }

    @Override
    public void advertiseLocalService(ServiceDescriptor descriptor) {
        discoveries.stream().forEach( discovery -> discovery.advertiseLocalService(descriptor));
    }

    @Override
    public void onReady(DiscoveryOnReady onReady) {
        discoveries.stream().forEach( discovery -> discovery.onReady(onReadyLatch::countDown));

        new Thread(() ->{
            try {
                onReadyLatch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                onReady.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void shutdown() {
        discoveries.stream().forEach(Discovery::shutdown);
    }
}
