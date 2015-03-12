package org.muoncore.extension.zeromq;

import org.muoncore.MuonExtension;
import org.muoncore.MuonExtensionApi;
import org.muoncore.extension.amqp.AmqpBroadcast;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

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
