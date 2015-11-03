package io.muoncore.protocol.introspection.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.descriptors.ServiceExtendedDescriptorSource;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class IntrospectionServerProtocolStack implements ServerProtocolStack {

    private ServiceExtendedDescriptorSource descriptorSource;
    private Codecs codecs;
    public final static String PROTOCOL = "introspect";

    public IntrospectionServerProtocolStack(ServiceExtendedDescriptorSource descriptorSource, Codecs codecs) {
        this.descriptorSource = descriptorSource;
        this.codecs = codecs;
    }

    @Override
    public ChannelConnection<TransportInboundMessage, TransportOutboundMessage> createChannel() {
        return new IntrospectionServerChannelConnection();
    }

    private class IntrospectionServerChannelConnection implements ChannelConnection<TransportInboundMessage, TransportOutboundMessage> {

        private ChannelFunction<TransportOutboundMessage> func;

        @Override
        public void receive(ChannelFunction<TransportOutboundMessage> function) {
            func = function;
        }

        @Override
        public void send(TransportInboundMessage message) {
            Codecs.EncodingResult result = codecs.encode(descriptorSource.getServiceExtendedDescriptor(), message.getSourceAvailableContentTypes().toArray(new String[0]));

            TransportOutboundMessage msg = new TransportOutboundMessage(
                    "introspectionReport",
                    UUID.randomUUID().toString(),
                    message.getSourceServiceName(),
                    message.getTargetServiceName(),
                    PROTOCOL,
                    new HashMap<>(),
                    result.getContentType(),
                    result.getPayload(),
                    Arrays.asList(codecs.getAvailableCodecs())
            );

            func.apply(msg);
        }
    }

    @Override
    public ProtocolDescriptor getProtocolDescriptor() {
        return new ProtocolDescriptor(
                PROTOCOL,
                "Introspection Protocol",
                "Provides the ability to introspect services to derive their capabilities. Built into most Muon implementations",
                Collections.emptyList());
    }
}
