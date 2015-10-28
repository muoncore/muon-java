package io.muoncore.protocol.reactivestream.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

public class ReactiveStreamServerChannel implements ChannelConnection<TransportInboundMessage, TransportOutboundMessage> {
    @Override
    public void receive(ChannelFunction<TransportOutboundMessage> function) {

    }

    @Override
    public void send(TransportInboundMessage message) {
        /**
         * subscribe
         * request
         * close
         */
    }
}
