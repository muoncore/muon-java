package io.muoncore;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface Discovery {
    /**
     * Lookup a remote service in the cache via the muon:// url scheme
     */
    default Optional<ServiceDescriptor> getService(URI uri) {
        if (!uri.getScheme().equals("muon")) {
            throw new IllegalArgumentException("Discovery requires muon://XXX scheme urls for lookup");
        }
        return findService(serviceDescriptor -> uri.getHost().equals(serviceDescriptor.getIdentifier()));
    }

    /**
     * Lookup a remote service in the cache via some predicate
     */
    default Optional<ServiceDescriptor> findService(Predicate<ServiceDescriptor> predicate) {
        return getKnownServices().stream().filter(predicate).findFirst();
    }

    /**
     * Return all of the services that are currently visible by this discovery mechanism
     * @return
     */
    List<ServiceDescriptor> getKnownServices();

    /**
     * Advertise a particular service.
     * If null, then nothing is advertised by the local process
     * @param descriptor
     */
    void advertiseLocalService(ServiceDescriptor descriptor);

    void onReady(Runnable onReady);
}
