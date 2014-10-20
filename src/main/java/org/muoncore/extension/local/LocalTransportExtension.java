package org.muoncore.extension.local;

import org.muoncore.MuonExtension;
import org.muoncore.MuonExtensionApi;

public class LocalTransportExtension implements MuonExtension {

    @Override
    public void init(MuonExtensionApi muonApi) {
        muonApi.addTransport(new LocalEventTransport());
    }

    @Override
    public String getName() {
        return "javalocal/1.0";
    }
}
