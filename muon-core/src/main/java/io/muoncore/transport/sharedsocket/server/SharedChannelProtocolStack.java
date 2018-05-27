package io.muoncore.transport.sharedsocket.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.channel.impl.ZipChannel;
import io.muoncore.codec.Codecs;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.descriptors.SchemaDescriptor;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.protocol.ServerStacks;

import java.util.Collections;
import java.util.Map;

public class SharedChannelProtocolStack implements ServerProtocolStack {

  public static final String STEP = "message";
  public static final String PROTOCOL = "shared-channel";
  private ServerStacks wrappedStacks;
  private Codecs codecs;

  public SharedChannelProtocolStack(ServerStacks wrappedStacks, Codecs codecs) {
    this.wrappedStacks = wrappedStacks;
    this.codecs = codecs;
  }

  @Override
  public Map<String, SchemaDescriptor> getSchemasFor(String endpoint) {
    return Collections.emptyMap();
  }

  @Override
  public ProtocolDescriptor getProtocolDescriptor() {
    return new ProtocolDescriptor("shared-channel", "Shared Channel", "Pipeline differing protocols over a shared, underlying channel",
      Collections.emptyList());
  }

  @Override
  public ChannelConnection<MuonInboundMessage, MuonOutboundMessage> createChannel() {

    ChannelConnection<MuonInboundMessage, MuonOutboundMessage> channelConnection = new SharedSocketServerChannel(wrappedStacks, codecs);

    ZipChannel zipChannel = Channels.zipChannel("server");

    Channels.connect(zipChannel.left(), channelConnection);

    return zipChannel.right();
  }
}
