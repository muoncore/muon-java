package io.muoncore.transport.sharedsocket.client.messages;

import io.muoncore.message.MuonOutboundMessage;

public class SharedChannelOutboundMessage {

    private String channelId;
    private MuonOutboundMessage message;

    public SharedChannelOutboundMessage(String channelId, MuonOutboundMessage message) {
        this.channelId = channelId;
        this.message = message;
    }

    public MuonOutboundMessage getMessage() {
        return message;
    }
    public String getChannelId() {
        return channelId;
    }
}
