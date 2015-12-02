package io.muoncore.protocol.reactivestream.client;

import io.muoncore.DiscoverySource;
import io.muoncore.codec.CodecsSource;
import io.muoncore.config.MuonConfigurationSource;
import io.muoncore.exception.MuonException;
import io.muoncore.transport.TransportClientSource;
import org.reactivestreams.Subscriber;

import java.io.UnsupportedEncodingException;
import java.net.URI;

public interface ReactiveStreamClientProtocolStack extends TransportClientSource, CodecsSource, MuonConfigurationSource, DiscoverySource {

    default <R> void subscribe(URI uri, Class<R> eventType, Subscriber<R> subscriber) throws UnsupportedEncodingException {
        if (!uri.getScheme().equals("stream")) throw new IllegalArgumentException("URI Scheme is invalid. Requires scheme: stream://");

        if (getDiscovery().findService( svc -> svc.getIdentifier().equals(uri.getHost())).isPresent()) {

            ReactiveStreamClientProtocol<R> proto = new ReactiveStreamClientProtocol<>(
                    uri,
                    getTransportClient().openClientChannel(),
                    subscriber,
                    eventType,
                    getCodecs(),
                    getConfiguration());

            proto.start();
        } else {
            throw new MuonException("The service " + uri.getHost() + " is not currently available");
        }
    }
}
