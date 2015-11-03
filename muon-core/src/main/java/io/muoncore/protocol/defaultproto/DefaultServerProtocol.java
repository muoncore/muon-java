package io.muoncore.protocol.defaultproto;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A default protocol intended to be the fallback if not other protocol is capable of processing an incoming message
 *
 * Responds back with a 404 message.
 */
public class DefaultServerProtocol implements ServerProtocolStack {

    private Codecs codecs;

    public DefaultServerProtocol(Codecs codecs) {
        this.codecs = codecs;
    }

    @Override
    public ChannelConnection<TransportInboundMessage, TransportOutboundMessage> createChannel() {
        return new DefaultServerChannelConnection();
    }

    private class DefaultServerChannelConnection implements ChannelConnection<TransportInboundMessage, TransportOutboundMessage> {

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
                        Arrays.asList(codecs.getAvailableCodecs())));
            }
        }
    }

    @Override
    public ProtocolDescriptor getProtocolDescriptor() {
        return new ProtocolDescriptor("default", "Default Protocol", "Returns 404 for all messages that match no other protocol", Collections.emptyList());
    }
}
