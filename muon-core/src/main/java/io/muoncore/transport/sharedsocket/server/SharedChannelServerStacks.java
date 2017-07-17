package io.muoncore.transport.sharedsocket.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.channel.impl.ZipChannel;
import io.muoncore.codec.Codecs;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerStacks;

public class SharedChannelServerStacks implements ServerStacks {

  public static final String STEP = "message";
  public static final String PROTOCOL = "shared-channel";
  private ServerStacks wrappedStacks;
    private Codecs codecs;

    public SharedChannelServerStacks(ServerStacks wrappedStacks, Codecs codecs) {
        this.wrappedStacks = wrappedStacks;
        this.codecs = codecs;
    }

    @Override
    public ChannelConnection<MuonInboundMessage, MuonOutboundMessage> openServerChannel(String protocol) {

        ChannelConnection<MuonInboundMessage, MuonOutboundMessage> channelConnection;

        if (protocol.equals(PROTOCOL)) {
            channelConnection = new SharedSocketServerChannel(wrappedStacks, codecs);
        } else {
            channelConnection = wrappedStacks.openServerChannel(protocol);
        }

        ZipChannel zipChannel = Channels.zipChannel("server");

        Channels.connect(zipChannel.left(), channelConnection);

        return zipChannel.right();
    }
}
