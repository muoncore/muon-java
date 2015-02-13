package org.muoncore.transports;

import org.muoncore.MuonStreamGenerator;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.net.URISyntaxException;
import java.util.Map;

public interface MuonStreamTransport extends MuonEventTransport {


    /**
     * Take data from the given publisher and will make it available for remote subscriptions
     */
    public void provideStreamSource(String streamName, MuonStreamGenerator sourceOfData);

    /**
     * Remotely subscribes to the remote stream and pushes data into the subscriber.
     */
    public void subscribeToStream(String url, Map<String, String> params, Subscriber subscriber) throws URISyntaxException;

}
