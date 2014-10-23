package org.muoncore.extension.http;

import java.util.List;

public interface HttpTransportServiceDiscovery {
    void register();
    //TODO, richer object needed here
    List<String> discover();
}
