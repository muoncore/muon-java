package io.muoncore.descriptors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProtocolDescriptor {
    private String protocolScheme;
    private String protocolName;
    private String description;
    private List<OperationDescriptor> operations;
}
