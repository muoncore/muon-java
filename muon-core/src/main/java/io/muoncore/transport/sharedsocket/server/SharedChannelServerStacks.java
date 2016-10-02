package io.muoncore.transport.sharedsocket.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.channel.impl.ZipChannel;
import io.muoncore.codec.Codecs;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.transport.sharedsocket.client.SharedSocketRouter;

public class SharedChannelServerStacks implements ServerStacks {

    private ServerStacks wrappedStacks;
    private Codecs codecs;

    public SharedChannelServerStacks(ServerStacks wrappedStacks, Codecs codecs) {
        this.wrappedStacks = wrappedStacks;
        this.codecs = codecs;
    }

    @Override
    public ChannelConnection<MuonInboundMessage, MuonOutboundMessage> openServerChannel(String protocol) {

        ChannelConnection<MuonInboundMessage, MuonOutboundMessage> channelConnection;

        if (protocol.equals(SharedSocketRouter.PROTOCOL)) {
            channelConnection = new SharedSocketServerChannel(wrappedStacks, codecs);
        } else {
            channelConnection = wrappedStacks.openServerChannel(protocol);
        }

        ZipChannel zipChannel = Channels.zipChannel("server");

        Channels.connect(zipChannel.left(), channelConnection);

        return zipChannel.right();
    }
}
