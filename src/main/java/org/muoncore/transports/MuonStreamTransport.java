package org.muoncore.transports;

import org.muoncore.MuonEventTransport;
import org.muoncore.MuonStreamGenerator;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.net.URISyntaxException;

public interface MuonStreamTransport extends MuonEventTransport {

    /**
     * Remote publishers will push data into the given subscriber
     */
    public void provideStreamSink(String streamName, Subscriber targetOfData);

    /**
     * Takes data from the given publisher and streams it to the remote subscriber
     */
    public void publishToStream(String url, Publisher publisher);

    /**
     * Take data from the given publisher and will make it available for remote subscriptions
     */
    public void provideStreamSource(String streamName, MuonStreamGenerator sourceOfData);

    /**
     * Remotely subscribes to the remote stream and pushes data into the subscriber.
     */
    public void subscribeToStream(String url, Subscriber subscriber) throws URISyntaxException;

}
