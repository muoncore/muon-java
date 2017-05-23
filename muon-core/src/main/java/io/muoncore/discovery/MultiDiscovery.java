package io.muoncore.discovery;

import io.muoncore.Discovery;
import io.muoncore.InstanceDescriptor;
import io.muoncore.ServiceDescriptor;

import java.util.List;
import java.util.Optional;
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
    public Optional<ServiceDescriptor> getServiceNamed(String name) {
      return discoveries.stream().map( it -> it.getServiceNamed(name))
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .findFirst();
    }

    @Override
    public List<String> getServiceNames() {
      return discoveries.stream().flatMap(it -> it.getServiceNames().stream()).distinct().collect(Collectors.toList());
    }

    @Override
    public Optional<ServiceDescriptor> getServiceWithTags(String... tags) {
      return discoveries.stream().map( it -> it.getServiceWithTags(tags))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

  @Override
    public void advertiseLocalService(InstanceDescriptor descriptor) {
        discoveries.forEach( discovery -> discovery.advertiseLocalService(descriptor));
    }

    @Override
    public void onReady(DiscoveryOnReady onReady) {
        discoveries.forEach( discovery -> discovery.onReady(onReadyLatch::countDown));

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
