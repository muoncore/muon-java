package org.muoncore.extension.zeromq;

import org.muoncore.MuonExtension;
import org.muoncore.MuonExtensionApi;

public class ZeroMQExtension implements MuonExtension {
    @Override
    public void init(MuonExtensionApi muonApi) {

    }

    @Override
    public String getName() {
        return "zeromq";
    }
}
