package io.muoncore.protocol.reactivestream.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class ReactiveStreamServerChannel implements ChannelConnection<TransportInboundMessage, TransportOutboundMessage> {

    private PublisherLookup publisherLookup;
    private Publisher publisher;

    public ReactiveStreamServerChannel(PublisherLookup publisherLookup) {
        this.publisherLookup = publisherLookup;
    }

    @Override
    public void receive(ChannelFunction<TransportOutboundMessage> function) {



    }

    @Override
    public void send(TransportInboundMessage message) {
        /**
         * subscribe
         * request
         * close
         * validate << remote is checking if the stream exists during handshake.
         */
    }

    void handleRequest() {



    }

    private void handleSubscribe() {

        publisherLookup.lookupPublisher("").subscribe(new Subscriber() {
            @Override
            public void onSubscribe(Subscription s) {

            }

            @Override
            public void onNext(Object o) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });

    }
}
