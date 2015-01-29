package org.muoncore;

import org.muoncore.transports.MuonEventTransport;

import java.net.URI;
import java.net.URL;
import java.util.List;

public class ServiceDescriptor {

    private String identifier;
    private List<String> tags;
    private List<URI> connectionUrls;

    public ServiceDescriptor(String identifier,
                             List<String> tags,
                             List<URI> connectionUrls) {
        this.identifier = identifier;
        this.tags = tags;
        this.connectionUrls = connectionUrls;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<URI> getConnectionUris() {
        return connectionUrls;
    }
}
