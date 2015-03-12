package org.muoncore.transports;

import org.muoncore.ServiceDescriptor;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransportList<T extends MuonEventTransport> {

    private List<T> transports = new ArrayList<T>();

    public void addTransport(T transport) {
        transports.add(transport);
    }

    public List<T> all() {
        return transports;
    }

    public T findBestResourceTransport(ServiceDescriptor descriptor) {

        //TODO, sorting of the uris to allow transport prioritisation
        for(URI uri: descriptor.getResourceConnectionUrls()) {
            for(T trans : transports) {
                if (uri.getScheme().equals(trans.getUrlScheme())) {
                    return trans;
                }
            }
        }

        return null;
    };

    public T findBestStreamTransport(ServiceDescriptor descriptor) {

        //TODO, sorting of the uris to allow transport prioritisation
        for(URI uri: descriptor.getStreamConnectionUrls()) {
            for(T trans : transports) {
                if (uri.getScheme().equals(trans.getUrlScheme())) {
                    return trans;
                }
            }
        }

        return null;
    };

    static Map<Class, String> capabilityMapping;

    static {
        capabilityMapping = new HashMap<Class, String>();
        capabilityMapping.put(MuonResourceTransport.class, "resource");
        capabilityMapping.put(MuonBroadcastTransport.class, "broadcast");
        capabilityMapping.put(MuonQueueTransport.class, "queue");
        capabilityMapping.put(MuonStreamTransport.class, "stream");
    }
}
