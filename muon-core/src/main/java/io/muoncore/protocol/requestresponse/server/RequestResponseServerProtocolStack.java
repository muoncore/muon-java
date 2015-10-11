package io.muoncore.protocol.requestresponse.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

/**
 * Server side of the Requestr Response protocol.
 *
 * Transports open channels on this protocol when a remote request response client opens a channel through them
 * and sends a first message.
 */
public class RequestResponseServerProtocolStack<X> implements ServerProtocolStack {

    @Override
    public ChannelConnection<TransportInboundMessage, TransportOutboundMessage> createChannel() {
        return new TransportInboundMessageTransportOutboundMessageChannelConnection();
    }

    private static class TransportInboundMessageTransportOutboundMessageChannelConnection implements ChannelConnection<TransportInboundMessage, TransportOutboundMessage> {

        private ChannelFunction<TransportOutboundMessage> function;

        @Override
        public void receive(ChannelFunction<TransportOutboundMessage> function) {
            this.function = function;
        }

        @Override
        public void send(TransportInboundMessage message) {
            //TODO, lookup the handlers
            function.apply(new TransportOutboundMessage("", message.getSourceServiceName(), message.getProtocol()));
        }
    }
}
