package org.muoncore;

import java.util.List;

public class ServiceDescriptor {

    private String identifier;
    private List<String> tags;
    private MuonEventTransport accessibleVia;

    public ServiceDescriptor(String identifier, List<String> tags, MuonEventTransport accessibleVia) {
        this.identifier = identifier;
        this.tags = tags;
        this.accessibleVia = accessibleVia;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getIdentifier() {
        return identifier;
    }

    public MuonEventTransport getAccessibleVia() {
        return accessibleVia;
    }
}
