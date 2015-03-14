package io.muoncore.extension.http;

import io.muoncore.MuonExtension;
import io.muoncore.MuonExtensionApi;

public class HttpTransportExtension implements MuonExtension {

    private int port;

    public HttpTransportExtension(int port) {
        this.port = port;
    }

    @Override
    public void init(MuonExtensionApi muonApi) {
        muonApi.addTransport(new HttpEventTransport(port));
    }

    @Override
    public String getName() {
        return "http/1.0";
    }
}
