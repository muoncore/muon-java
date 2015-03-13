package org.muoncore.extension.zeromq;

import org.muoncore.MuonStreamGenerator;
import org.muoncore.codec.TransportCodecType;
import org.muoncore.transports.MuonStreamTransport;
import org.reactivestreams.Subscriber;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class ZeroMQTransport implements MuonStreamTransport {

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
    public URI getLocalConnectionURI() throws URISyntaxException {
        return new URI("zeromq://localhost");
    }

    @Override
    public TransportCodecType getCodecType() {
        return TransportCodecType.BINARY;
    }
}
