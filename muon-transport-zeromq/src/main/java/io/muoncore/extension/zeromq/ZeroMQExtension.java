package io.muoncore.extension.zeromq;

import io.muoncore.MuonExtension;
import io.muoncore.MuonExtensionApi;

public class ZeroMQExtension implements MuonExtension {
    @Override
    public void init(MuonExtensionApi muonApi) {

    }

    @Override
    public String getName() {
        return "zeromq";
    }
}
