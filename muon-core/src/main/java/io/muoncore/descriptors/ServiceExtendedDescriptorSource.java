package io.muoncore.descriptors;

import io.muoncore.protocol.introspection.SchemaIntrospectionRequest;

public interface ServiceExtendedDescriptorSource {
    ServiceExtendedDescriptor getServiceExtendedDescriptor();
    SchemasDescriptor getSchemasDescriptor(SchemaIntrospectionRequest request);
}
