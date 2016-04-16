package io.muoncore.protocol.introspection.server;

import io.muoncore.Discovery;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.descriptors.ServiceExtendedDescriptor;
import io.muoncore.descriptors.ServiceExtendedDescriptorSource;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerProtocolStack;

import java.util.Collections;

public class IntrospectionServerProtocolStack implements ServerProtocolStack {

    private ServiceExtendedDescriptorSource descriptorSource;
    private Codecs codecs;
    private Discovery discovery;
    public final static String PROTOCOL = "introspect";

    public IntrospectionServerProtocolStack(ServiceExtendedDescriptorSource descriptorSource, Codecs codecs, Discovery discovery) {
        this.descriptorSource = descriptorSource;
        this.codecs = codecs;
        this.discovery = discovery;
    }

    @Override
    public ChannelConnection<MuonInboundMessage, MuonOutboundMessage> createChannel() {
        return new IntrospectionServerChannelConnection();
    }

    private class IntrospectionServerChannelConnection implements ChannelConnection<MuonInboundMessage, MuonOutboundMessage> {

        private ChannelFunction<MuonOutboundMessage> func;

        @Override
        public void receive(ChannelFunction<MuonOutboundMessage> function) {
            func = function;
        }

        @Override
        public void send(MuonInboundMessage message) {
            if (message == null) {
                func.apply(null);
                return;
            }

            ServiceExtendedDescriptor descriptor = descriptorSource.getServiceExtendedDescriptor();

            Codecs.EncodingResult result = codecs.encode(descriptor,
                    discovery.getCodecsForService(message.getSourceServiceName()));

            func.apply(MuonMessageBuilder
                    .fromService(descriptor.getServiceName())
                    .step("introspectionReport")
                    .protocol(PROTOCOL)
                    .toService(message.getSourceServiceName())
                    .payload(result.getPayload())
                    .contentType(result.getContentType())
                    .status(MuonMessage.Status.success)
                    .build()
            );
        }

        @Override
        public void shutdown() {
            if (func != null) {
                func.apply(null);
                func = null;
            }
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
