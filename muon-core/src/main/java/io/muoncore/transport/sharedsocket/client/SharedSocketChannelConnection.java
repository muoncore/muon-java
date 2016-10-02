package io.muoncore.transport.sharedsocket.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.transport.sharedsocket.client.messages.SharedChannelOutboundMessage;

import java.util.UUID;

public class SharedSocketChannelConnection implements ChannelConnection<MuonOutboundMessage, MuonInboundMessage> {

    private ChannelFunction<MuonInboundMessage> inboundFunction;
    private ChannelFunction<SharedChannelOutboundMessage> outboundFunction;
    private String channelId = UUID.randomUUID().toString();
    private Codecs codecs;

    public SharedSocketChannelConnection(Codecs codecs, ChannelConnection.ChannelFunction<SharedChannelOutboundMessage> outboundFunction) {
        this.outboundFunction = outboundFunction;
        this.codecs = codecs;
    }

    @Override
    public void receive(ChannelFunction<MuonInboundMessage> function) {
        this.inboundFunction = function;
    }

    @Override
    public void send(MuonOutboundMessage message) {
        SharedChannelOutboundMessage sharedMessage = new SharedChannelOutboundMessage(channelId, message);
        outboundFunction.apply(sharedMessage);
    }

    public void sendInbound(MuonInboundMessage message) {
        inboundFunction.apply(message);
    }

    public String getChannelId() {
        return channelId;
    }

    @Override
    public void shutdown() {

    }
}
