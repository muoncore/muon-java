package io.muoncore;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

public interface Discovery {
    /**
     * Lookup a remote service in the cache via the muon:// url scheme
     */
    default Optional<ServiceDescriptor> getService(URI uri) {
        if (!uri.getScheme().equals("muon")) {
            throw new IllegalArgumentException("Discovery requires muon://XXX scheme urls for lookup");
        }
        return getServiceNamed(uri.getHost());
    }

    /**
     * Lookup a remote service in the cache via some predicate
     */
//    default Optional<ServiceDescriptor> findService(Predicate<ServiceDescriptor> predicate) {
//        return getKnownServices().stream().filter(predicate).findFirst();
//    }

    List<String> getServiceNames();
    Optional<ServiceDescriptor> getServiceNamed(String name);
    Optional<ServiceDescriptor> getServiceWithTags(String...tags);

    default String[] getCodecsForService(String name) {
        Optional<ServiceDescriptor> service = getServiceNamed(name);
        if (service.isPresent()) {
            return service.get().getCodecs();
        }
        return new String[] { "application/json" };
    }

    /**
     * Return all of the services that are currently visible by this discovery mechanism
     * @return
     */
//    List<ServiceDescriptor> getKnownServices();

    /**
     * Advertise a particular service.
     * If null, then nothing is advertised by the local process
     * @param descriptor
     */
    void advertiseLocalService(InstanceDescriptor descriptor);

    void onReady(DiscoveryOnReady onReady);

    void shutdown();

    default void blockUntilReady() {
        CountDownLatch latch = new CountDownLatch(1);
        onReady(latch::countDown);
        synchronized (this) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    interface DiscoveryOnReady {
        void call();
    }
}
