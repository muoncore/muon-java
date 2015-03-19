package io.muoncore.transport.stream;

import io.muoncore.MuonStreamGenerator;
import io.muoncore.transport.MuonEventTransport;
import org.reactivestreams.Subscriber;

import java.net.URISyntaxException;
import java.util.Map;

public interface MuonStreamTransport extends MuonEventTransport {


    /**
     * Take data from the given publisher and will make it available for remote subscriptions
     */
    public <T> void provideStreamSource(String streamName, MuonStreamGenerator<T> sourceOfData);

    /**
     * Remotely subscribes to the remote stream and pushes data into the subscriber.
     */
    public <T> void subscribeToStream(String url, Class<T> type, Map<String, String> params, Subscriber<T> subscriber) throws URISyntaxException;

}
