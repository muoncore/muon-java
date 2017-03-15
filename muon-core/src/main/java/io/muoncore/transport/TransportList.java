package io.muoncore.transport;

import io.muoncore.ServiceDescriptor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class TransportList<T extends MuonTransport> {

    private List<T> transports = new ArrayList<T>();

    public void addTransport(T transport) {
        transports.add(transport);
    }

    public List<T> all() {
        return transports;
    }

    public T findBestTransport(ServiceDescriptor descriptor) {
        for(String scheme: descriptor.getSchemes()) {
            for(T trans : transports) {
                if (scheme.equals(trans.getUrlScheme())) {
                    return trans;
                }
            }
        }

        return null;
    }
}
