package io.muoncore.extension.http;

import io.muoncore.MuonExtension;
import io.muoncore.MuonExtensionApi;
import io.muoncore.MuonService;

public class HttpTransportExtension implements MuonExtension {

    private int port;

    public HttpTransportExtension(int port) {
        this.port = port;
    }

    @Override
    public void extend(MuonService muonApi) {
        muonApi.registerTransport(new HttpEventTransport(port));
    }
}
