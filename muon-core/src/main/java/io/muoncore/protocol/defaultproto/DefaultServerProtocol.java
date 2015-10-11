package io.muoncore.protocol.defaultproto;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

public class DefaultServerProtocol implements ServerProtocolStack {
    @Override
    public ChannelConnection<TransportInboundMessage, TransportOutboundMessage> createChannel() {
        return new DefaultServerChannelConnection();
    }

    private static class DefaultServerChannelConnection implements ChannelConnection<TransportInboundMessage, TransportOutboundMessage> {

        private ChannelFunction<TransportOutboundMessage> func;

        @Override
        public void receive(ChannelFunction<TransportOutboundMessage> function) {
            func = function;
        }

        @Override
        public void send(TransportInboundMessage message) {
            if (func != null) {
                func.apply(new TransportOutboundMessage(message.getId() + "REPLY", message.getSourceServiceName(), message.getProtocol(), true));
            }
        }
    }
}
