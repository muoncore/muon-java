package org.muoncore;

public class ServiceDescriptor {

    private String identifier;
    private MuonEventTransport accessibleVia;

    public ServiceDescriptor(String identifier, MuonEventTransport accessibleVia) {
        this.identifier = identifier;
        this.accessibleVia = accessibleVia;
    }

    public String getIdentifier() {
        return identifier;
    }

    public MuonEventTransport getAccessibleVia() {
        return accessibleVia;
    }
}
