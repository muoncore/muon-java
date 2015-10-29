package io.muoncore.descriptors;

import java.util.List;

public class ServiceExtendedDescriptor {

    private String serviceName;
    private List<ProtocolDescriptor> protocols;

    public ServiceExtendedDescriptor(String serviceName, List<ProtocolDescriptor> protocols) {
        this.serviceName = serviceName;
        this.protocols = protocols;
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<ProtocolDescriptor> getProtocols() {
        return protocols;
    }
}
