package io.muoncore.descriptors;

import java.util.List;

public class ProtocolDescriptor {
    private String protocolScheme;
    private String protocolName;
    private String description;
    private List<OperationDescriptor> operations;


    public ProtocolDescriptor(String protocolScheme, String protocolName, String description, List<OperationDescriptor> operations) {
        this.protocolScheme = protocolScheme;
        this.protocolName = protocolName;
        this.description = description;
        this.operations = operations;
    }

    public String getProtocolScheme() {
        return protocolScheme;
    }

    public String getDescription() {
        return description;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public List<OperationDescriptor> getOperations() {
        return operations;
    }
}
