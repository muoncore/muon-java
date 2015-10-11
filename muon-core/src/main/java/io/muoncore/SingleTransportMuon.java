package io.muoncore;

import io.muoncore.protocol.DynamicRegistrationServerProtocols;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.protocol.defaultproto.DefaultServerProtocol;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.client.SingleTransportClient;
import io.muoncore.transport.client.TransportClient;

/**
 * Simple bundle of default Muon protocol stacks based on a single transport.
 */
public class SingleTransportMuon implements Muon
{

    private TransportClient transportClient;
    private Discovery discovery;
    private ServerStacks protocols;

    public SingleTransportMuon(
            Discovery discovery,
            MuonTransport transport) {
        this.transportClient = new SingleTransportClient(transport);
        this.discovery = discovery;
        this.protocols = new DynamicRegistrationServerProtocols(new DefaultServerProtocol());
    }



//    @Override
//    public Discovery getDiscovery() {
//        return discovery;
//    }
//
//    @Override
//    public AutoConfiguration getConfiguration() {
//        return null;
//    }


    @Override
    public TransportClient getTransportClient() {
        return transportClient;
    }
}
