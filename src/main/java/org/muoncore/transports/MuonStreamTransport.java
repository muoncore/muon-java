package org.muoncore.transports;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.net.URISyntaxException;

public interface MuonStreamTransport {

    //TODO, methods to discover the streams available

    public void publishStream(String url, Publisher pub);
    public void subscribeToStream(String url, Subscriber subscriber) throws URISyntaxException;
}
