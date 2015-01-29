package org.muoncore.transports;

import org.muoncore.ServiceDescriptor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class TransportList<T extends MuonEventTransport> {

    private List<T> transports = new ArrayList<T>();

    public void addTransport(T transport) {
        transports.add(transport);
    }

    public List<T> all() {
        return transports;
    }

    public T findBestTransport(ServiceDescriptor descriptor) {

        //TODO, sorting of the uris to allow transport prioritisation
        for(URI uri: descriptor.getConnectionUris()) {
            for(T trans : transports) {
                if (uri.getScheme().equals(trans.getUrlScheme())) {
                    return trans;
                }
            }
        }

        return null;
    };
}
