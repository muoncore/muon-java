package org.muoncore.transports;

import org.muoncore.codec.TransportCodecType;

import java.net.URI;
import java.net.URISyntaxException;

public interface MuonEventTransport {

    public void shutdown();

    public void start() throws Exception;

    public String getUrlScheme();

    public URI getLocalConnectionURI() throws URISyntaxException;

    public TransportCodecType getCodecType();
}
