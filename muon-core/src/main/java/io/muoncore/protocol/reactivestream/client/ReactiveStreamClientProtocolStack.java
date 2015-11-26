package io.muoncore.protocol.reactivestream.client;

import io.muoncore.codec.CodecsSource;
import io.muoncore.config.MuonConfigurationSource;
import io.muoncore.transport.TransportClientSource;
import org.reactivestreams.Subscriber;

import java.io.UnsupportedEncodingException;
import java.net.URI;

public interface ReactiveStreamClientProtocolStack extends TransportClientSource, CodecsSource, MuonConfigurationSource {

    default <R> void subscribe(URI uri, Class<R> eventType, Subscriber<R> subscriber) throws UnsupportedEncodingException {
        if (!uri.getScheme().equals("stream")) throw new IllegalArgumentException("URI Scheme is invalid. Requires scheme: stream://");
        ReactiveStreamClientProtocol<R> proto = new ReactiveStreamClientProtocol<>(
                uri,
                getTransportClient().openClientChannel(),
                subscriber,
                eventType,
                getCodecs(),
                getConfiguration());

        proto.start();
    }
}
