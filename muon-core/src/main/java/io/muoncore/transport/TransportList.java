package io.muoncore.transport;

import io.muoncore.ServiceDescriptor;
import io.muoncore.transport.*;
import io.muoncore.transport.crud.requestresponse.MuonResourceTransport;
import io.muoncore.transport.crud.stream.MuonStreamTransport;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransportList<T extends MuonTransport> {

    private List<T> transports = new ArrayList<T>();

    public void addTransport(T transport) {
        transports.add(transport);
    }

    public List<T> all() {
        return transports;
    }

    public T findBestTransport(ServiceDescriptor descriptor) {
        for(URI uri: descriptor.getConnectionUrls()) {
            for(T trans : transports) {
                if (uri.getScheme().equals(trans.getUrlScheme())) {
                    return trans;
                }
            }
        }

        return null;
    };
}
