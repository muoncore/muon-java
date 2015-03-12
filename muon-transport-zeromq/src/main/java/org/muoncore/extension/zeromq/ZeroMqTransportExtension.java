package org.muoncore.extension.zeromq;

import org.muoncore.MuonExtension;
import org.muoncore.MuonExtensionApi;

public class ZeroMqTransportExtension implements MuonExtension {

    @Override
    public void init(MuonExtensionApi muonApi) {
        ZeroMqEventTransport trans = new ZeroMqEventTransport(
                muonApi.getMuon().getServiceIdentifer(),
                muonApi.getTags());

        muonApi.addTransport(trans);
    }

    @Override
    public String getName() {
        return "zeromq/1.0";
    }
}
