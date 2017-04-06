package io.muoncore.descriptors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServiceExtendedDescriptor {
    private String serviceName;
    private List<ProtocolDescriptor> protocols;
}
