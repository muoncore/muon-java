package io.muoncore.protocol.reactivestream.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

public class ReactiveStreamServerStack implements ServerProtocolStack {

    public static String REACTIVE_STREAM_PROTOCOL = "reactive-stream";

    @Override
    public ChannelConnection<TransportInboundMessage, TransportOutboundMessage> createChannel() {
        return new ReactiveStreamServerChannel();
    }

    @Override
    public ProtocolDescriptor getProtocolDescriptor() {
        return null;
    }
}
