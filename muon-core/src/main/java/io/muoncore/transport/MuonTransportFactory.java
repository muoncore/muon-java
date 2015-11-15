package io.muoncore.transport;

import java.util.Properties;

public interface MuonTransportFactory {

    String SERVICE_NAME_PROPERTY_NAME = "serviceName";

    MuonTransport build(Properties properties);
}
