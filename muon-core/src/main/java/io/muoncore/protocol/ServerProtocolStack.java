package io.muoncore.protocol;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

/**
 * The server side of a communication protocol.
 * Channels will be established from a remote by the transport system based
 * on the incoming requested protocol.
 *
 * Often paired with some kind of API to configure the stack.
 */
public interface ServerProtocolStack {
    ProtocolDescriptor getProtocolDescriptor();
    ChannelConnection<TransportInboundMessage, TransportOutboundMessage> createChannel();
}
