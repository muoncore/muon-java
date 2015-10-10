package io.muoncore;

import io.muoncore.config.AutoConfiguration;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.client.SingleTransportClient;
import io.muoncore.transport.client.TransportClient;

public class SingleTransportMuon implements Muon
{

    private TransportClient transportClient;
    private Discovery discovery;

    public SingleTransportMuon(
            Discovery discovery,
            MuonTransport transport) {
        this.transportClient = new SingleTransportClient(transport);
        this.discovery = discovery;
    }

    @Override
    public Discovery getDiscovery() {
        return discovery;
    }

    @Override
    public AutoConfiguration getConfiguration() {
        return null;
    }

    @Override
    public TransportClient getTransportClient() {
        return transportClient;
    }
}
