package io.muoncore.protocol;

import io.muoncore.Discovery;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.transport.client.TransportClient;

public abstract class ClientProtocolFactory<API> {

    public abstract API create(
            Discovery discovery,
            AutoConfiguration configuration,
            TransportClient transportClient);
}
