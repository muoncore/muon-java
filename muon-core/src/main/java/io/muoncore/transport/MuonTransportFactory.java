package io.muoncore.transport;

import io.muoncore.config.AutoConfiguration;

import java.util.Properties;

public interface MuonTransportFactory {

    MuonTransport build(Properties properties);

    void setAutoConfiguration(AutoConfiguration autoConfiguration);
}
