package io.muoncore.protocol.defaultproto;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * A default protocol intended to be the fallback if not other protocol is capable of processing an incoming message
 *
 * Responds back with a 404 message.
 */
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
                Map<String, String> metadata = new HashMap<>();
                metadata.put("status", "404");
                metadata.put("message", "Protocol unknown :" + message.getProtocol());
                func.apply(new TransportOutboundMessage(
                        "errorSent",
                        message.getId() + "REPLY",
                        message.getTargetServiceName(),
                        message.getSourceServiceName(),
                        message.getProtocol(),
                        metadata,
                        "text/plain",
                        new byte[0],
                        true));
            }
        }
    }
}
