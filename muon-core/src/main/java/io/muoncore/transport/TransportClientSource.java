package io.muoncore.transport;

import io.muoncore.transport.client.TransportClient;

public interface TransportClientSource {
    TransportClient getTransportClient();
}
