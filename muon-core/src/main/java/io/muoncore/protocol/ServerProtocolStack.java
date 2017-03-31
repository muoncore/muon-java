package io.muoncore.protocol;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.descriptors.SchemaDescriptor;
import io.muoncore.descriptors.SchemasDescriptor;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;

import java.util.Map;

/**
 * The server side of a communication protocol.
 * Channels will be established from a remote by the transport system based
 * on the incoming requested protocol.
 * <p>
 * Often paired with some kind of API to configure the stack.
 */
public interface ServerProtocolStack {
  Map<String, SchemaDescriptor> getSchemasFor(String endpoint);

  ProtocolDescriptor getProtocolDescriptor();

  ChannelConnection<MuonInboundMessage, MuonOutboundMessage> createChannel();
}
