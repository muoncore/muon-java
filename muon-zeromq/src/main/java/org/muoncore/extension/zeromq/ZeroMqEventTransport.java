package org.muoncore.extension.zeromq;

import org.muoncore.MuonStreamGenerator;
import org.muoncore.codec.TransportCodecType;
import org.muoncore.transports.*;
import org.reactivestreams.Subscriber;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ZeroMqEventTransport
        implements
        MuonStreamTransport {

    private Logger log = Logger.getLogger(ZeroMqEventTransport.class.getName());

    private String serviceName;

    public ZeroMqEventTransport(String serviceName, List<String> tags){
        this.serviceName = serviceName;

    }

    @Override
    public void provideStreamSource(String streamName, MuonStreamGenerator sourceOfData) {

    }

    @Override
    public void subscribeToStream(String url, Map<String, String> params, Subscriber subscriber) throws URISyntaxException {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public String getUrlScheme() {
        return "zeromq";
    }

    @Override
    public TransportCodecType getCodecType() {
        return TransportCodecType.BINARY;
    }

    @Override
    public URI getLocalConnectionURI() throws URISyntaxException {
        return new URI("zeromq://localhost:2213");
    }
}
