package io.muoncore.transport.sharedsocket.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.transport.sharedsocket.client.messages.SharedChannelOutboundMessage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.UUID;

@Slf4j
public class SharedSocketChannelConnection implements ChannelConnection<MuonOutboundMessage, MuonInboundMessage> {

    private ChannelFunction<MuonInboundMessage> inboundFunction;
    private ChannelFunction<SharedChannelOutboundMessage> outboundFunction;
    private String channelId = UUID.randomUUID().toString();
    private Runnable onShutdown;

    public SharedSocketChannelConnection(Codecs codecs, ChannelConnection.ChannelFunction<SharedChannelOutboundMessage> outboundFunction, Runnable onShutdown) {
        this.outboundFunction = outboundFunction;
        this.onShutdown = onShutdown;
    }

    @Override
    public void receive(ChannelFunction<MuonInboundMessage> function) {
        this.inboundFunction = function;
    }

    @Override
    public void send(MuonOutboundMessage message) {
        val sharedMessage = new SharedChannelOutboundMessage(channelId, message);
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
      onShutdown.run();
    }
}
